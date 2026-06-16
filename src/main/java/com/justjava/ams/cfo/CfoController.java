package com.justjava.ams.cfo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cfo")
public class CfoController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "cfo/dashboard";
    }
}
