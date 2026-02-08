package com.gntcyouthbe.user.domain;

import com.gntcyouthbe.church.domain.Church;
import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.user.model.request.UserNameUpdateRequest;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_user",
    uniqueConstraints = {
            @UniqueConstraint(name="uk_user_email", columnNames = {"email"}),
            @UniqueConstraint(name="uk_provider_uid", columnNames = {"provider", "providerUserId"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String email;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(nullable = false, length = 200)
    private String providerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", referencedColumnName = "id", foreignKey =  @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Church church;

    public User(String email, String name, AuthProvider provider, String providerUserId, Role role) {
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.role = role;
    }

    public User(String email, String name, AuthProvider provider, String providerUserId) {
        this(email, name, provider, providerUserId, Role.USER);
    }

    public String getRoleName() {
        return "ROLE_" + this.role.name();
    }

    public ChurchId getChurchId() {
        return church != null ? church.getId() : null;
    }

    public void updateName(UserNameUpdateRequest request) {
        this.name = request.getNewName();
    }

    public void updateNameAndChurch(String name, Church church) {
        this.name = name;
        this.church = church;
    }
}
