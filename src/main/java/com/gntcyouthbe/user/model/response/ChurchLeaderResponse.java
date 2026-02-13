package com.gntcyouthbe.user.model.response;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.user.domain.User;
import lombok.Getter;

@Getter
public class ChurchLeaderResponse {

    private final String churchId;
    private final String churchName;
    private final LeaderInfo leader;

    private ChurchLeaderResponse(String churchId, String churchName, LeaderInfo leader) {
        this.churchId = churchId;
        this.churchName = churchName;
        this.leader = leader;
    }

    public static ChurchLeaderResponse of(ChurchId churchId, User leader) {
        return new ChurchLeaderResponse(
                churchId.name(),
                churchId.getDisplayName(),
                leader != null ? LeaderInfo.from(leader) : null
        );
    }

    @Getter
    public static class LeaderInfo {
        private final Long id;
        private final String name;
        private final String email;

        private LeaderInfo(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        static LeaderInfo from(User user) {
            return new LeaderInfo(user.getId(), user.getName(), user.getEmail());
        }
    }
}
