package com.gntcyouthbe.user.model.response;

import java.util.List;
import lombok.Getter;

@Getter
public class AdminUserListResponse {

    private final List<AdminUserResponse> users;
    private final long totalElements;
    private final int totalPages;
    private final int page;
    private final int size;

    public AdminUserListResponse(List<AdminUserResponse> users, long totalElements,
                                  int totalPages, int page, int size) {
        this.users = users;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }
}
