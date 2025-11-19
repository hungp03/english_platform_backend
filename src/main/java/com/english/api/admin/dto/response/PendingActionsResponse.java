package com.english.api.admin.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingActionsResponse {
    
    private Long instructorRequestsCount;
    private Long forumReportsCount;
    private Long pendingOrdersCount;
    
}
