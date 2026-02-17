package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.OtpEmailEvent;
import com.ealth.codeleat.dtos.PasswordResetEmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

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


    @EventListener
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void handlePasswordResetEmailEvent(PasswordResetEmailEvent event) {
        if (event.isAddPassword()) {
            log.info("Sending add password email to {} for {} OAuth",
                    event.getEmail(), event.getOAuthProvider());
            resendEmailService.sendAddPasswordEmail(
                    event.getEmail(),
                    event.getVerificationLink(),
                    event.getOAuthProvider()
            );
        } else {
            log.info("Sending reset password email to {}", event.getEmail());
            resendEmailService.sendResetPasswordEmail(
                    event.getEmail(),
                    event.getVerificationLink()
            );
        }
    }

    @Recover
    public void recover(Exception e, OtpEmailEvent event) {
        log.error("Failed to send OTP email to {} after all retries", event.getEmail(), e);
    }

    @Recover
    public void recover(Exception e, PasswordResetEmailEvent event) {
        log.error("Failed to send OTP email to {} after all retries", event.getEmail(), e);
    }
}
