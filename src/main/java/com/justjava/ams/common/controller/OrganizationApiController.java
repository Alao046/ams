package com.justjava.ams.common.controller;

import com.justjava.ams.common.dto.OrganizationCreateRequest;
import com.justjava.ams.common.dto.OrganizationDTO;
import com.justjava.ams.common.dto.OrganizationUpdateRequest;
import com.justjava.ams.common.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationApiController {
    private final OrganizationService organizationService;

    @GetMapping
    public List<OrganizationDTO> getOrganizations() {
        return organizationService.getAllOrganizations();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationDTO createOrganization(@Valid @RequestBody OrganizationCreateRequest request) {
        return organizationService.createOrganization(request);
    }

    @GetMapping("/{id}")
    public OrganizationDTO getOrganization(@PathVariable Long id) {
        return organizationService.getOrganization(id);
    }

    @PutMapping("/{id}")
    public OrganizationDTO updateOrganization(@PathVariable Long id, @Valid @RequestBody OrganizationUpdateRequest request) {
        return organizationService.updateOrganization(id, request);
    }
}
