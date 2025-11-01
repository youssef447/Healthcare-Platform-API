package com.healthcare.ingestion.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/ingestion")
public class IngestionViewController {




    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public String showUploadPage(@AuthenticationPrincipal OidcUser user, Model model) {
        model.addAttribute("username", user != null ? user.getAttribute("preferred_username") : "User");
        return "ingestion/upload";
    }


}
