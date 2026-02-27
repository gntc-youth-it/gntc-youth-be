package com.gntcyouthbe.user.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.file.domain.UploadedFile;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.gntcyouthbe.user.model.request.UserProfileRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profile",
    uniqueConstraints = {
            @UniqueConstraint(name = "uk_user_profile_user_id", columnNames = {"user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(nullable = false)
    private Integer generation;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UploadedFile profileImage;

    public UserProfile(User user, Integer generation, String phoneNumber, Gender gender) {
        this.user = user;
        this.generation = generation;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    public void update(UserProfileRequest request) {
        this.generation = request.getGeneration();
        this.phoneNumber = request.getPhoneNumber();
        this.gender = request.getGender();
    }

    public void updateProfileImage(UploadedFile profileImage) {
        this.profileImage = profileImage;
    }
}
