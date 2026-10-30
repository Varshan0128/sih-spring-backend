package org.aicte.approval.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.notification.model.AuditEvent;
import org.aicte.approval.notification.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Transactional
    public AuditEvent recordEvent(String action, String entityName, String entityId,
                                   String previousState, String newState, String correlationId) {
        Optional<UserPrincipal> currentUser = SecurityUtils.getCurrentUser();
        Long actorId = currentUser.map(UserPrincipal::getId).orElse(null);
        String username = currentUser.map(UserPrincipal::getUsername).orElse("SYSTEM");
        String role = currentUser.map(u -> u.getAuthorities().isEmpty() ? "ROLE_SYSTEM" :
                u.getAuthorities().iterator().next().getAuthority()).orElse("ROLE_SYSTEM");

        String corrId = (correlationId != null && !correlationId.isBlank()) ?
                correlationId : UUID.randomUUID().toString();

        Instant now = Instant.now();

        // SHA-256 Tamper-Evident Hash Chaining
        String prevHash = auditEventRepository.findTopByOrderByIdDesc()
                .map(AuditEvent::getRecordHash)
                .orElse("0000000000000000000000000000000000000000000000000000000000000000");

        String payloadToHash = prevHash + "|" + corrId + "|" + username + "|" + action + "|"
                + entityName + "|" + entityId + "|" + now.toString();
        String currentHash = computeSha256(payloadToHash);

        AuditEvent event = AuditEvent.builder()
                .correlationId(corrId)
                .actorUserId(actorId)
                .actorUsername(username)
                .actorRole(role)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .previousState(previousState)
                .newState(newState)
                .ipAddress("127.0.0.1")
                .timestamp(now)
                .recordHash(currentHash)
                .build();

        return auditEventRepository.save(event);
    }

    public List<AuditEvent> getEntityTimeline(String entityName, String entityId) {
        return auditEventRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
    }

    public List<AuditEvent> getRecentEvents(int limit) {
        return auditEventRepository.findAll(org.springframework.data.domain.PageRequest.of(0, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"))).getContent();
    }

    public java.util.Map<String, Object> verifyChainIntegrity() {
        List<AuditEvent> events = auditEventRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
        boolean valid = true;
        String prevHash = "0000000000000000000000000000000000000000000000000000000000000000";

        for (AuditEvent ev : events) {
            String expectedPayload = prevHash + "|" + ev.getCorrelationId() + "|" + ev.getActorUsername() + "|"
                    + ev.getAction() + "|" + ev.getEntityName() + "|" + ev.getEntityId() + "|" + ev.getTimestamp().toString();
            String expectedHash = computeSha256(expectedPayload);
            if (!expectedHash.equals(ev.getRecordHash())) {
                valid = false;
                break;
            }
            prevHash = ev.getRecordHash();
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("chainValid", valid);
        result.put("totalEvents", events.size());
        result.put("algorithm", "SHA-256");
        result.put("tamperStatus", valid ? "SECURE_UNMODIFIED" : "INTEGRITY_COMPROMISED");
        return result;
    }

    private String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return UUID.randomUUID().toString().replace("-", "");
        }
    }
}
