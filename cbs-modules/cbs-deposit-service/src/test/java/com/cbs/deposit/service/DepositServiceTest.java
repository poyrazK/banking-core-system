package com.cbs.deposit.service;

import com.cbs.common.exception.ApiException;
import com.cbs.deposit.dto.AccrueInterestRequest;
import com.cbs.deposit.dto.CreateDepositRequest;
import com.cbs.deposit.dto.DepositResponse;
import com.cbs.deposit.dto.DepositStatusReasonRequest;
import com.cbs.deposit.model.DepositAccount;
import com.cbs.deposit.model.DepositProductType;
import com.cbs.deposit.model.DepositStatus;
import com.cbs.deposit.repository.DepositAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private DepositAccountRepository depositAccountRepository;

    private DepositService depositService;

    @BeforeEach
    void setUp() {
        depositService = new DepositService(depositAccountRepository);
    }

    @Test
    void createDeposit_normalizesDepositNumber() {
        CreateDepositRequest request = new CreateDepositRequest(
                1L,
                10L,
                "  dep-001  ",
                DepositProductType.TERM,
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(18.0),
                90,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 5, 2)
        );
        when(depositAccountRepository.existsByDepositNumber("DEP-001")).thenReturn(false);
        when(depositAccountRepository.save(any(DepositAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DepositResponse response = depositService.createDeposit(request);

        assertEquals("DEP-001", response.depositNumber());
        assertEquals(DepositStatus.OPEN, response.status());
        assertEquals(BigDecimal.valueOf(20000), response.currentAmount());
    }

    @Test
    void createDeposit_throwsWhenDepositNumberExists() {
        CreateDepositRequest request = new CreateDepositRequest(
                1L,
                10L,
                "DEP-001",
                DepositProductType.TERM,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(15.0),
                30,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 3)
        );
        when(depositAccountRepository.existsByDepositNumber("DEP-001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> depositService.createDeposit(request));

        assertEquals("DEPOSIT_NUMBER_EXISTS", exception.getErrorCode());
    }

    @Test
    void accrueInterest_increasesCurrentAmount() {
        DepositAccount account = createDepositWithStatus(DepositStatus.OPEN);
        when(depositAccountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(depositAccountRepository.save(any(DepositAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DepositResponse response = depositService.accrueInterest(11L, new AccrueInterestRequest(BigDecimal.valueOf(250)));

        assertEquals(BigDecimal.valueOf(20250), response.currentAmount());
    }

    @Test
    void closeDeposit_throwsWhenStatusNotMatured() {
        DepositAccount account = createDepositWithStatus(DepositStatus.OPEN);
        when(depositAccountRepository.findById(12L)).thenReturn(Optional.of(account));

        ApiException exception = assertThrows(ApiException.class, () -> depositService.closeDeposit(12L));

        assertEquals("DEPOSIT_NOT_MATURED", exception.getErrorCode());
    }

    @Test
    void breakDeposit_setsBrokenStatusAndReason() {
        DepositAccount account = createDepositWithStatus(DepositStatus.OPEN);
        when(depositAccountRepository.findById(13L)).thenReturn(Optional.of(account));
        when(depositAccountRepository.save(any(DepositAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DepositResponse response = depositService.breakDeposit(13L, new DepositStatusReasonRequest("  emergency  "));

        assertEquals(DepositStatus.BROKEN, response.status());
        assertEquals("emergency", response.statusReason());
    }

    private DepositAccount createDepositWithStatus(DepositStatus status) {
        DepositAccount account = new DepositAccount(
                1L,
                10L,
                "DEP-100",
                DepositProductType.TERM,
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(18.0),
                90,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 5, 2)
        );
        account.setStatus(status);
        return account;
    }
}
