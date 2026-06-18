package com.justjava.ams.auditor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auditor")
public class AuditorController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "auditor/dashboard";
    }

    @GetMapping("/auditLogExplorer")
    public String auditLogExplorer() {
        return "auditor/auditLogExplorer";
    }

    @GetMapping("/securityEvents")
    public String securityEvents() {
        return "auditor/securityEvents";
    }
}