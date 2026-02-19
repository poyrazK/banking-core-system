package com.cbs.account.dto;

import com.cbs.account.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(@NotNull AccountStatus status) {
}
