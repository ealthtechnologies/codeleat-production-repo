package com.ealth.codeleat.services;

public interface RateLimitService {
    public boolean tryConsume(String key);
}
