package com.justjava.ams.financeAdmin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/financeAdmin")
public class FinanceAdminController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "financeAdmin/dashboard";
    }

    @GetMapping("/organizationSetup")
    public String organizationSetup() {
        return "financeAdmin/organizationSetup";
    }

    @GetMapping("/chartOfAccounts")
    public String chartOfAccounts() {
        return "financeAdmin/chartOfAccounts";
    }

    @GetMapping("/fiscalConfiguration")
    public String fiscalConfiguration() {
        return "financeAdmin/fiscalConfiguration";
    }

    @GetMapping("/moduleControls")
    public String moduleControls() {
        return "financeAdmin/moduleControls";
    }

    @GetMapping("/taxJurisdictions")
    public String taxJurisdictions() {
        return "financeAdmin/taxJurisdictions";
    }
}