package com.sergeev.taskmanager.company.internal.mapper;

import com.sergeev.taskmanager.company.api.dto.CompanyMembershipDto;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMembershipMapper {
    @Mapping(target = "role", expression = "java(membership.getRole().getName())")
    @Mapping(target = "companyId", expression = "java(membership.getCompany().getId())")
    CompanyMembershipDto toDto (CompanyMembership membership);
}
