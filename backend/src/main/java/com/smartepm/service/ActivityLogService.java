package com.smartepm.service;

import com.smartepm.dto.response.ActivityLogResponse;
import com.smartepm.dto.response.PageResponse;

public interface ActivityLogService {

    /** Records one audit entry. Uses the currently authenticated principal as the actor when available. */
    void log(String action, String entityType, Long entityId, String description);

    PageResponse<ActivityLogResponse> search(String entityType, String actorUsername, int page, int size);
}
