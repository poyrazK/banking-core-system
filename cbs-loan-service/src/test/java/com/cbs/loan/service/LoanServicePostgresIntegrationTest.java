package com.cbs.loan.service;

import com.cbs.loan.dto.CreateLoanRequest;
import com.cbs.loan.dto.LoanRepaymentRequest;
import com.cbs.loan.dto.LoanResponse;
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
                "jdbc:postgresql://localhost:55436/cbs_loan_it"
        ));
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
                LocalDate.of(2027, 2, 1)
        ));

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
                LocalDate.of(2026, 8, 1)
        ));

        loanService.approveLoan(created.id());
        loanService.disburseLoan(created.id());
        LoanResponse closed = loanService.repayLoan(created.id(), new LoanRepaymentRequest(BigDecimal.valueOf(5000)));

        assertEquals(LoanStatus.CLOSED, closed.status());
        assertEquals(0, closed.outstandingAmount().compareTo(BigDecimal.ZERO));
        Loan persisted = loanRepository.findById(created.id()).orElseThrow();
        assertEquals(LoanStatus.CLOSED, persisted.getStatus());
    }
}