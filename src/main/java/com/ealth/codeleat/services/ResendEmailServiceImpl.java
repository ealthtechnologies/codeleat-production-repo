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

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Your Codeleat Reset Password Link")
                .html(buildVerificationLinkEmailHtml(resetLink))
                .build();

        try {
            SendEmailResponse response = resend.emails().send(emailRequest);
            System.out.println("Email sent! ID: " + response.getId());
        } catch (ResendException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendAddPasswordEmail(String toEmail, String resetLink, String provider) {
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Your Codeleat Add Password Link")
                .html(buildVerificationLinkOAuthEmailHtml(resetLink, provider))
                .build();

        try {
            SendEmailResponse response = resend.emails().send(emailRequest);
            System.out.println("Email sent! ID: " + response.getId());
        } catch (ResendException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private String buildVerificationLinkEmailHtml(String resetLink) {
        return """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto;">
                <h2>Reset Your Password</h2>
                
                <p>We received a request to reset your password.</p>
                
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s"
                       style="
                           background-color: #4CAF50;
                           color: white;
                           padding: 14px 24px;
                           text-decoration: none;
                           font-size: 16px;
                           border-radius: 5px;
                           display: inline-block;
                       ">
                        Reset Password
                    </a>
                </div>
                
                <p style="color: #666;">
                    This link will expire in 5 minutes.
                </p>

                <p style="color: #666; font-size: 14px;">
                    If the button doesn't work, copy and paste the link below into your browser:
                </p>

                <p style="word-break: break-all; font-size: 12px; color: #999;">
                    %s
                </p>

                <p style="color: #999; font-size: 12px; margin-top: 20px;">
                    If you didn't request a password reset, you can safely ignore this email.
                </p>
            </div>
        </body>
        </html>
        """.formatted(resetLink, resetLink);
    }

    private String buildVerificationLinkOAuthEmailHtml(String resetLink, String provider) {
        return """
    <!DOCTYPE html>
    <html>
    <body style="font-family: Arial, sans-serif; padding: 20px;">
        <div style="max-width: 600px; margin: 0 auto;">
            <h2>Add a Password to Your Account</h2>
            
            <p>Hi there!</p>
            
            <p>You requested to add a password to your account that was created using <strong>%s Sign-In</strong>.</p>
            
            <p>Adding a password will give you more flexibility - you'll be able to sign in using either your email/password or %s in the future.</p>
            
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s"
                   style="
                       background-color: #4CAF50;
                       color: white;
                       padding: 14px 24px;
                       text-decoration: none;
                       font-size: 16px;
                       border-radius: 5px;
                       display: inline-block;
                   ">
                    Set Password
                </a>
            </div>
            
            <p style="color: #666;">
                This link will expire in 5 minutes.
            </p>

            <p style="color: #666; font-size: 14px;">
                If the button doesn't work, copy and paste the link below into your browser:
            </p>

            <p style="word-break: break-all; font-size: 12px; color: #999;">
                %s
            </p>

            <p style="color: #999; font-size: 12px; margin-top: 20px;">
                If you didn't request this, you can safely ignore this email. Your account will remain accessible via %s Sign-In.
            </p>
        </div>
    </body>
    </html>
    """.formatted(provider, provider, resetLink, resetLink, provider);
    }
}
