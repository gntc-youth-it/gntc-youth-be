package com.gntcyouthbe.cell.domain;

import com.gntcyouthbe.user.domain.User;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CellMembers {
    private final List<CellMember> members;

    public List<User> getUsers() {
        return members.stream()
                .map(CellMember::getUser)
                .toList();
    }

    public int getMemberCount() {
        return members.size();
    }
}
