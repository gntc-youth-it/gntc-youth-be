package com.gntcyouthbe.user.model.response;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import lombok.Getter;

@Getter
public class AdminUserResponse {

    private final String name;
    private final String churchName;
    private final Integer generation;
    private final String phoneNumber;
    private final String role;

    private AdminUserResponse(String name, String churchName, Integer generation,
                              String phoneNumber, String role) {
        this.name = name;
        this.churchName = churchName;
        this.generation = generation;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public static AdminUserResponse from(User user, UserProfile profile) {
        ChurchId church = user.getChurchId();
        return new AdminUserResponse(
                user.getName(),
                church != null ? church.getDisplayName() : null,
                profile != null ? profile.getGeneration() : null,
                profile != null ? maskPhoneNumber(profile.getPhoneNumber()) : null,
                user.getRole().name()
        );
    }

    private static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        // 010-1234-5678 → 010-****-5678
        return phoneNumber.replaceAll("(\\d{3})-?(\\d{3,4})-?(\\d{4})", "$1-****-$3");
    }
}
