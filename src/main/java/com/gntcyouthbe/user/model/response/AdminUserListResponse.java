package com.gntcyouthbe.user.model.response;

import java.util.List;
import lombok.Getter;

@Getter
public class AdminUserListResponse {

    private final List<AdminUserResponse> users;

    public AdminUserListResponse(List<AdminUserResponse> users) {
        this.users = users;
    }
}
