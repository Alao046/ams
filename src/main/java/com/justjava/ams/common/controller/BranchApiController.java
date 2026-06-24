package com.justjava.ams.common.controller;

import com.justjava.ams.common.dto.BranchCreateRequest;
import com.justjava.ams.common.dto.BranchDTO;
import com.justjava.ams.common.dto.BranchUpdateRequest;
import com.justjava.ams.common.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchApiController {
    private final BranchService branchService;

    @GetMapping
    public List<BranchDTO> getBranches(@RequestParam Long organizationId) {
        return branchService.getBranchesByOrganization(organizationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchDTO createBranch(@Valid @RequestBody BranchCreateRequest request) {
        return branchService.createBranch(request);
    }

    @GetMapping("/{id}")
    public BranchDTO getBranch(@PathVariable Long id) {
        return branchService.getBranch(id);
    }

    @PutMapping("/{id}")
    public BranchDTO updateBranch(@PathVariable Long id, @Valid @RequestBody BranchUpdateRequest request) {
        return branchService.updateBranch(id, request);
    }
}
