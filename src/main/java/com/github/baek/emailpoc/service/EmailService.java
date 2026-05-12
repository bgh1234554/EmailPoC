package com.github.baek.emailpoc.service;

public interface EmailService {
    void sendActivationEmail(String to, String activationLink, int expireMinutes);
}
