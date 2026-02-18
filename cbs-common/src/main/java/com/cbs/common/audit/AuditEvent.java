package com.cbs.common.audit;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record AuditEvent(
        String action,
        String resourceType,
        String resourceId,
        String actorType,
        String actorId,
        AuditOutcome outcome,
        Instant occurredAt,
        Map<String, Object> details
) {

    public AuditEvent {
        action = Objects.requireNonNull(action, "action must not be null");
        resourceType = Objects.requireNonNull(resourceType, "resourceType must not be null");
        outcome = Objects.requireNonNull(outcome, "outcome must not be null");
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        details = details == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }

    public static AuditEvent of(String action,
                                String resourceType,
                                String resourceId,
                                String actorType,
                                String actorId,
                                AuditOutcome outcome,
                                Map<String, Object> details) {
        return new AuditEvent(action, resourceType, resourceId, actorType, actorId, outcome, Instant.now(), details);
    }
}
