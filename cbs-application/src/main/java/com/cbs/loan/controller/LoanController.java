package com.cbs.loan.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.cbs.common.api.ApiResponse;
import com.cbs.loan.dto.CreateLoanRequest;
import com.cbs.loan.dto.LoanDecisionRequest;
import com.cbs.loan.dto.LoanRepaymentRequest;
import com.cbs.loan.dto.LoanResponse;
import com.cbs.loan.dto.LoanScheduleResponse;
import com.cbs.loan.model.LoanStatus;
import com.cbs.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "Loans", description = "Endpoints for loan application, repayment, and status management")
public class LoanController {

    private final LoanService loanService;
    private final com.cbs.loan.service.LoanRepaymentService loanRepaymentService;

    public LoanController(LoanService loanService, com.cbs.loan.service.LoanRepaymentService loanRepaymentService) {
        this.loanService = loanService;
        this.loanRepaymentService = loanRepaymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LoanResponse>> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.ok(ApiResponse.success("Loan created", response));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoan(@PathVariable("loanId") Long loanId) {
        LoanResponse response = loanService.getLoan(loanId);
        return ResponseEntity.ok(ApiResponse.success("Loan retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanResponse>>> listLoans(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "status", required = false) LoanStatus status) {
        List<LoanResponse> responses = loanService.listLoans(customerId, status);
        return ResponseEntity.ok(ApiResponse.success("Loans retrieved", responses));
    }

    @PatchMapping("/{loanId}/approve")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(@PathVariable("loanId") Long loanId) {
        LoanResponse response = loanService.approveLoan(loanId);
        return ResponseEntity.ok(ApiResponse.success("Loan approved", response));
    }

    @PatchMapping("/{loanId}/reject")
    public ResponseEntity<ApiResponse<LoanResponse>> rejectLoan(
            @PathVariable("loanId") Long loanId,
            @Valid @RequestBody LoanDecisionRequest request) {
        LoanResponse response = loanService.rejectLoan(loanId, request);
        return ResponseEntity.ok(ApiResponse.success("Loan rejected", response));
    }

    @PatchMapping("/{loanId}/disburse")
    public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(@PathVariable("loanId") Long loanId) {
        LoanResponse response = loanService.disburseLoan(loanId);
        return ResponseEntity.ok(ApiResponse.success("Loan disbursed", response));
    }

    @PatchMapping("/{loanId}/repay")
    public ResponseEntity<ApiResponse<LoanResponse>> repayLoan(
            @PathVariable("loanId") Long loanId,
            @Valid @RequestBody LoanRepaymentRequest request) {
        LoanResponse response = loanService.repayLoan(loanId, request);
        return ResponseEntity.ok(ApiResponse.success("Loan repayment posted", response));
    }

    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<ApiResponse<LoanScheduleResponse>> getLoanSchedule(
            @PathVariable("loanId") Long loanId) {
        LoanScheduleResponse response = loanService.getSchedule(loanId);
        return ResponseEntity.ok(ApiResponse.success("Loan schedule retrieved", response));
    }

    @PostMapping("/jobs/repayment:trigger")
    public ResponseEntity<ApiResponse<Integer>> triggerDailyRepayment(
            @RequestParam(value = "date", required = false) java.time.LocalDate date) {
        java.time.LocalDate processDate = date != null ? date : java.time.LocalDate.now();
        int count = loanRepaymentService.processDueInstallments(processDate);
        return ResponseEntity.ok(ApiResponse.success("Daily loan repayment triggered successfully", count));
    }
}
