package com.springweb.model;

public record PaymentRequest(String user, Double amount, int inSeconds) {
}
