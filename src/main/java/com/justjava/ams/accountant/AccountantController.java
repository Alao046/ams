package com.justjava.ams.accountant;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accountant")
public class AccountantController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "accountant/dashboard";
    }
}
