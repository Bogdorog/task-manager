package com.sergeev.taskmanager.company.internal.service;

import com.sergeev.taskmanager.company.api.CompanyApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyApiImpl implements CompanyApi {

    private final CompanyService companyService;

    @Override
    public String getCompanyNameById(Long companyId)
    {
        return companyService.getCompanyNameById(companyId);
    }
}
