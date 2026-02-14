package com.ealth.codeleat.services;


public interface ResendEmailService {
    public void sendOtpEmail(String toEmail, String otp);
    public void sendResetPasswordEmail(String toEmail, String resetLink);
    public void sendAddPasswordEmail(String toEmail, String resetLink, String provider);
}
