package com.sergeev.taskmanager.company.api.dto;

public record CompanyDto(
        Long id,
        String name,
        String description,
        String email,
        String phone,
        String address
) {}
