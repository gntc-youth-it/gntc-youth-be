package com.gntcyouthbe.user.model.response;

import com.gntcyouthbe.user.domain.UserProfile;
import lombok.Getter;

@Getter
public class UserProfileResponse {

    private final Integer generation;
    private final String phoneNumber;
    private final String gender;
    private final String genderDisplay;

    private UserProfileResponse(Integer generation, String phoneNumber, String gender, String genderDisplay) {
        this.generation = generation;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.genderDisplay = genderDisplay;
    }

    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getGeneration(),
                profile.getPhoneNumber(),
                profile.getGender().name(),
                profile.getGender().getDisplayName()
        );
    }
}
