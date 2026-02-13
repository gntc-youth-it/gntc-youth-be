package com.gntcyouthbe.user.model.response;

import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import lombok.Getter;

@Getter
public class UserRoleUpdateResponse {

    private final Long userId;
    private final String name;
    private final String role;
    private final String churchName;
    private final DemotedLeaderInfo previousLeader;

    private UserRoleUpdateResponse(Long userId, String name, String role, String churchName,
            DemotedLeaderInfo previousLeader) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.churchName = churchName;
        this.previousLeader = previousLeader;
    }

    public static UserRoleUpdateResponse of(User user, User demotedLeader) {
        return new UserRoleUpdateResponse(
                user.getId(),
                user.getName(),
                user.getRole().name(),
                user.getChurch() != null ? user.getChurch().getId().getDisplayName() : null,
                demotedLeader != null ? DemotedLeaderInfo.from(demotedLeader) : null
        );
    }

    @Getter
    public static class DemotedLeaderInfo {
        private final Long id;
        private final String name;
        private final String role;

        private DemotedLeaderInfo(Long id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }

        static DemotedLeaderInfo from(User user) {
            return new DemotedLeaderInfo(user.getId(), user.getName(), user.getRole().name());
        }
    }
}
