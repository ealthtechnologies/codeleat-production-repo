package com.ealth.codeleat.services;


public interface ResendEmailService {
    public void sendOtpEmail(String toEmail, String otp);
}
