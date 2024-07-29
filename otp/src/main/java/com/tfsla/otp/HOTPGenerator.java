package com.tfsla.otp;

public interface HOTPGenerator {
    String generate(long counter) throws IllegalArgumentException;
}
