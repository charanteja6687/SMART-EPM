package com.smartepm.service.impl;
import com.smartepm.dto.response.ActivityLogResponse;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.entity.ActivityLog;
import com.smartepm.repository.ActivityLogRepository;
import com.smartepm.security.UserPrincipal;
import com.smartepm.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, String description) {
        try {
            ActivityLog entry = ActivityLog.builder()
                    .actorUsername(currentUsername())
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .build();

            activityLogRepository.save(entry);

        } catch (Exception ex) {
             // Audit logging is best-effort.
        }
    }

    @Override
    public PageResponse<ActivityLogResponse> search(String entityType, String actorUsername, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ActivityLog> result = activityLogRepository.search(
                (entityType == null || entityType.isBlank()) ? null : entityType,
                (actorUsername == null || actorUsername.isBlank()) ? null : actorUsername,
                pageable);

        List<ActivityLogResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<ActivityLogResponse>builder()
                .content(content)
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUsername();
        }
        return "SYSTEM";
    }

    private ActivityLogResponse toResponse(ActivityLog a) {
        return ActivityLogResponse.builder()
                .id(a.getId())
                .actorUsername(a.getActorUsername())
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .description(a.getDescription())
                .timestamp(a.getTimestamp())
                .build();
    }
}
