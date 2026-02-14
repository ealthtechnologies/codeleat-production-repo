package com.ealth.codeleat.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpEmailEventListener {

    private final ResendEmailService resendEmailService;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void handleOtpEmailEvent(OtpEmailEvent event) {
        log.info("Sending OTP email to {} for {} (attempt)", event.getEmail(), event.getEventType());
        resendEmailService.sendOtpEmail(event.getEmail(), event.getOtp());
        log.info("OTP email sent successfully to {}", event.getEmail());
    }

    @Recover
    public void recover(Exception e, OtpEmailEvent event) {
        log.error("Failed to send OTP email to {} after all retries", event.getEmail(), e);
        // Optionally: Store in DB for manual intervention
    }
}
