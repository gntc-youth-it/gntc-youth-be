package com.gntcyouthbe.user.model.response;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import lombok.Getter;

@Getter
public class UserProfileResponse {

    private final String name;
    private final String churchId;
    private final String churchName;
    private final Integer generation;
    private final String phoneNumber;
    private final String gender;
    private final String genderDisplay;

    private UserProfileResponse(String name, String churchId, String churchName,
                                Integer generation, String phoneNumber, String gender, String genderDisplay) {
        this.name = name;
        this.churchId = churchId;
        this.churchName = churchName;
        this.generation = generation;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.genderDisplay = genderDisplay;
    }

    public static UserProfileResponse from(User user, UserProfile profile) {
        ChurchId church = user.getChurchId();
        return new UserProfileResponse(
                user.getName(),
                church != null ? church.name() : null,
                church != null ? church.getDisplayName() : null,
                profile != null ? profile.getGeneration() : null,
                profile != null ? profile.getPhoneNumber() : null,
                profile != null && profile.getGender() != null ? profile.getGender().name() : null,
                profile != null && profile.getGender() != null ? profile.getGender().getDisplayName() : null
        );
    }
}
