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

    // 이름과 사진만 저장한 프로필(PATCH /user/profile)은 기수·전화번호·성별이 비어 있다.
    // ddl-auto: update는 이미 걸린 NOT NULL을 풀지 않으므로, 운영 DB는 직접 ALTER한다.
    private Integer generation;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
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

    // 프로필 행이 없는 사람이 사진만 저장할 때 쓴다
    public UserProfile(User user) {
        this.user = user;
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
