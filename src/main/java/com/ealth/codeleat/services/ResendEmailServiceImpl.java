package com.ealth.codeleat.services;

import com.resend.*;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailServiceImpl implements ResendEmailService{
    private final Resend resend;
    private final String fromEmail;

    public ResendEmailServiceImpl(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.email}") String fromEmail
    ) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Your Codeleat Verification Code")
                .html(buildOtpEmailHtml(otp))
                .build();

        try {
            SendEmailResponse response = resend.emails().send(emailRequest);
            System.out.println("Email sent! ID: " + response.getId());
        } catch (ResendException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private String buildOtpEmailHtml(String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2>Verify Your Email</h2>
                    <p>Your verification code is:</p>
                    <div style="background: #f4f4f4; padding: 15px; font-size: 24px; 
                                letter-spacing: 5px; text-align: center; font-weight: bold;">
                        %s
                    </div>
                    <p style="color: #666; margin-top: 20px;">
                        This code will expire in 5 minutes.
                    </p>
                    <p style="color: #999; font-size: 12px;">
                        If you didn't request this code, please ignore this email.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(otp);
    }
}
