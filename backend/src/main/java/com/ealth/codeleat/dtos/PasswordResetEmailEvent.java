package com.ealth.codeleat.dtos;

import lombok.Getter;

// Create new event
@Getter
public class PasswordResetEmailEvent {
    private final String email;
    private final String verificationLink;
    private final String oAuthProvider; // null if regular password reset

    public PasswordResetEmailEvent(String email, String verificationLink, String oAuthProvider) {
        this.email = email;
        this.verificationLink = verificationLink;
        this.oAuthProvider = oAuthProvider;
    }

    public boolean isAddPassword() {
        return oAuthProvider != null;
    }
}
