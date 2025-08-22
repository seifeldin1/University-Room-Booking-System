package com.example.University.dto;

import lombok.Data;

@Data
public class BuildingDto {
    private String name;
    private String code;
    private String address;
    private Integer totalFloors;
    private Long departmentId;
}
