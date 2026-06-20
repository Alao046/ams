package com.justjava.ams.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BranchController {

	// Simple MVC controller to serve organization/branch setup UI
	@GetMapping("/finance-admin/organization-setup")
	public String organizationSetup() {
		return "financeAdmin/organizationSetup";
	}
}


