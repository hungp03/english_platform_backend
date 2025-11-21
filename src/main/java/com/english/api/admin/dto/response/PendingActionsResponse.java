package com.english.api.admin.dto.response;

import lombok.*;

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
