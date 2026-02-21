package com.cbs.loan.model;

public enum AmortizationType {
    ANNUITY, // Equal total payment each period
    FLAT, // Interest on original principal
    REDUCING_BALANCE // Equal principal + decreasing interest
}
