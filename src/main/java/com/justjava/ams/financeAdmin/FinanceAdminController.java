package com.justjava.ams.financeAdmin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/finance-admin")
public class FinanceAdminController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "financeAdmin/dashboard";
    }
}
