package com.cbs.common.audit;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class AuditLogHelper {

    private AuditLogHelper() {
    }

    public static AuditEvent success(String action,
                                     String resourceType,
                                     String resourceId,
                                     String actorType,
                                     String actorId,
                                     Map<String, Object> details) {
        return AuditEvent.of(action, resourceType, resourceId, actorType, actorId, AuditOutcome.SUCCESS, details);
    }

    public static AuditEvent failure(String action,
                                     String resourceType,
                                     String resourceId,
                                     String actorType,
                                     String actorId,
                                     Map<String, Object> details) {
        return AuditEvent.of(action, resourceType, resourceId, actorType, actorId, AuditOutcome.FAILURE, details);
    }

    public static Map<String, Object> toStructuredFields(AuditEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("audit.action", event.action());
        fields.put("audit.resourceType", event.resourceType());
        if (event.resourceId() != null) {
            fields.put("audit.resourceId", event.resourceId());
        }
        if (event.actorType() != null) {
            fields.put("audit.actorType", event.actorType());
        }
        if (event.actorId() != null) {
            fields.put("audit.actorId", event.actorId());
        }
        fields.put("audit.outcome", event.outcome().name());
        fields.put("audit.occurredAt", event.occurredAt().toString());
        if (!event.details().isEmpty()) {
            fields.put("audit.details", event.details());
        }

        return Collections.unmodifiableMap(fields);
    }
}
