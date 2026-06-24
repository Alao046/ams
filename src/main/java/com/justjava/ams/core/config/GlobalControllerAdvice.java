package com.justjava.ams.core.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalControllerAdvice {

    @Autowired
    private AuthenticationManager authenticationManager;

    @ModelAttribute("currentPath")
    public String getCurrentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("userName")
    public String addUserName(HttpServletRequest request) {
        String firstName = claimAsString("given_name");
        String lastName = claimAsString("family_name");
        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        String name = claimAsString("name");
        if (!name.isBlank()) {
            return name;
        }

        String username = claimAsString("preferred_username");
        return !username.isBlank() ? username : "User";
    }

    private String claimAsString(String claimName) {
        Object claim = authenticationManager.get(claimName);
        return claim != null ? claim.toString().trim() : "";
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException ex) {

        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {

        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);

        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }
}
