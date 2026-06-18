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

    @GetMapping("/manualJournal")
    public String manualJournal() {
        return "accountant/manualJournal";
    }

    @GetMapping("/purchaseInvoice")
    public String purchaseInvoice() {
        return "accountant/purchaseInvoice";
    }

    @GetMapping("/customerInvoicing")
    public String customerInvoicing() {
        return "accountant/customerInvoicing";
    }

    @GetMapping("/cashAndBank")
    public String cashAndBank() {
        return "accountant/cashAndBank";
    }

    @GetMapping("/fixedAssets")
    public String fixedAssets() {
        return "accountant/fixedAssets";
    }
}