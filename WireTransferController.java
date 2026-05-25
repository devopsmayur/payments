package com.acme.payments.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wire-transfers")
public class WireTransferController {

    @PostMapping("/quote")
    @ResponseStatus(HttpStatus.OK)
    public WireTransferQuoteResponse quote(@Valid @RequestBody WireTransferQuoteRequest request) {
        boolean requiresManualReview = request.amount().compareTo(new BigDecimal("25000")) >= 0;
        boolean requiresStepUpAuth = request.amount().compareTo(new BigDecimal("10000")) >= 0;

        BigDecimal fee = request.amount().compareTo(new BigDecimal("5000")) >= 0
                ? new BigDecimal("25.00")
                : new BigDecimal("10.00");

        return new WireTransferQuoteResponse(
                UUID.randomUUID().toString(),
                request.currency(),
                request.amount(),
                fee,
                request.amount().add(fee),
                requiresStepUpAuth,
                requiresManualReview,
                requiresManualReview ? List.of("HIGH_VALUE_TRANSFER") : List.of(),
                OffsetDateTime.now().plusMinutes(10).toString()
        );
    }

    public record WireTransferQuoteRequest(
            @NotBlank String sourceAccountId,
            @NotBlank String beneficiaryId,
            @NotBlank String currency,
            @DecimalMin("1.00") BigDecimal amount
    ) {}

    public record WireTransferQuoteResponse(
            String quoteId,
            String currency,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal totalDebit,
            boolean requiresStepUpAuth,
            boolean requiresManualReview,
            List<String> complianceFlags,
            String expiresAt
    ) {}
}
