package com.cbs.loan.service;

import com.cbs.loan.dto.CreateLoanRequest;
import com.cbs.loan.dto.LoanRepaymentRequest;
import com.cbs.loan.dto.LoanResponse;
import com.cbs.loan.model.AmortizationType;
import com.cbs.loan.model.Loan;
import com.cbs.loan.model.LoanStatus;
import com.cbs.loan.model.LoanType;
import com.cbs.loan.repository.LoanRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LoanServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55436/cbs_loan_it"));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    @AfterEach
    void cleanUp() {
        loanRepository.deleteAll();
    }

    @Test
    void createLoanPersistsNormalizedLoanNumberInPostgres() {
        LoanResponse response = loanService.createLoan(new CreateLoanRequest(
                1L,
                10L,
                "  loan-001  ",
                LoanType.PERSONAL,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(12.5),
                12,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2027, 2, 1),
                AmortizationType.ANNUITY));

        assertEquals("LOAN-001", response.loanNumber());
        assertTrue(loanRepository.existsByLoanNumber("LOAN-001"));
    }

    @Test
    void disburseAndRepayPersistStatusAndOutstandingAmount() {
        LoanResponse created = loanService.createLoan(new CreateLoanRequest(
                2L,
                20L,
                "LOAN-002",
                LoanType.MORTGAGE,
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(10.5),
                6,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 8, 1),
                AmortizationType.ANNUITY));

        loanService.approveLoan(created.id());
        loanService.disburseLoan(created.id());
        LoanResponse closed = loanService.repayLoan(created.id(), new LoanRepaymentRequest(BigDecimal.valueOf(5000)));

        assertEquals(LoanStatus.CLOSED, closed.status());
        assertEquals(0, closed.outstandingAmount().compareTo(BigDecimal.ZERO));
        Loan persisted = loanRepository.findById(created.id()).orElseThrow();
        assertEquals(LoanStatus.CLOSED, persisted.getStatus());
    }

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.cbs.account.service.AccountService accountService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.cbs.ledger.service.LedgerPostingService ledgerPostingService;

    @Autowired
    private LoanRepaymentService loanRepaymentService;

    @Test
    void automatedRepaymentEngineDeductsBalanceCorrectly() {
        LoanResponse created = loanService.createLoan(new CreateLoanRequest(
                3L,
                30L,
                "LOAN-003",
                LoanType.PERSONAL,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10),
                12,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(11),
                AmortizationType.FLAT));

        loanService.approveLoan(created.id());
        loanService.disburseLoan(created.id());

        com.cbs.account.dto.AccountResponse mockAccountResponse = new com.cbs.account.dto.AccountResponse(
                30L, 3L, "ACC-002", com.cbs.account.model.AccountType.SAVINGS,
                com.cbs.account.model.Currency.USD, com.cbs.account.model.AccountStatus.ACTIVE,
                BigDecimal.valueOf(10000));

        org.mockito.Mockito.when(accountService.getAccount(30L)).thenReturn(mockAccountResponse);

        int processed = loanRepaymentService.processDueInstallments(LocalDate.now());

        assertTrue(processed > 0);
        org.mockito.Mockito.verify(accountService, org.mockito.Mockito.atLeastOnce())
                .debitBalance(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.verify(ledgerPostingService, org.mockito.Mockito.atLeastOnce())
                .postEntry(org.mockito.ArgumentMatchers.any());
    }
}