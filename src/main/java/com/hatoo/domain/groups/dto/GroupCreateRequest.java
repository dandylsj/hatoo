package com.hatoo.domain.groups.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class GroupCreateRequest {

    private String name;

    private String description;

    private UUID assignerId;

}
