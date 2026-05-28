package com.lni.datalni.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sends bare hits on {@code /} to {@code /index.zul} (Spring Security then routes
 * unauthenticated users to {@code /login.zul}).
 */
@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String home() {
        return "redirect:/index.zul";
    }
}
