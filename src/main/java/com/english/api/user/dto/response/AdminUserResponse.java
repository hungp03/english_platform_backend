
// package com.english.api.user.dto.response;

// import java.util.UUID;
// import java.time.Instant;
// import java.util.Set;

// public class AdminUserResponse {
//     private UUID id;
//     private String fullName;
//     private String email;
//     private boolean active;
//     private Instant createdAt;
//     private Set<String> roles;

//     public UUID getId() { return id; }
//     public void setId(UUID id) { this.id = id; }
//     public String getFullName() { return fullName; }
//     public void setFullName(String fullName) { this.fullName = fullName; }
//     public String getEmail() { return email; }
//     public void setEmail(String email) { this.email = email; }
//     public boolean isActive() { return active; }
//     public void setActive(boolean active) { this.active = active; }
//     public Instant getCreatedAt() { return createdAt; }
//     public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
//     public Set<String> getRoles() { return roles; }
//     public void setRoles(Set<String> roles) { this.roles = roles; }
// }
package com.english.api.user.dto.response;

import java.util.List;
import java.util.UUID;
import java.time.Instant;
public record AdminUserResponse(
        UUID id,
        String fullName,
        String email,
        boolean active,
        Instant createdAt,
        List<String> roles
) {
}
