package com.akeshya.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private boolean status;
    private String createdAt;
    private String updatedAt;
}
