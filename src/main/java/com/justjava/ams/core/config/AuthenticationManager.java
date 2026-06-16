package com.justjava.ams.core.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationManager {
    public Object get(String fieldName){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
        return defaultOidcUser.getClaims().get(fieldName);
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    public boolean isAdmin() {
        List<String> groups = (List<String>) this.get("groups");
        if (groups == null) {
            return false;
        }
        return groups.contains("/admin");
    }

    public boolean isAccountant() {
        List<String> groups = (List<String>) this.get("groups");
        if (groups == null) {
            return false;
        }
        return groups.contains("/accountant");
    }

    public boolean isAuditor() {
        List<String> groups = (List<String>) this.get("groups");
        if (groups == null) {
            return false;
        }
        return groups.contains("/auditor");
    }

    public boolean isCfo() {
        List<String> groups = (List<String>) this.get("groups");
        if (groups == null) {
            return false;
        }
        return groups.contains("/cfo");
    }

    public boolean isFinanceAdmin() {
        List<String> groups = (List<String>) this.get("groups");
        if (groups == null) {
            return false;
        }
        return groups.contains("/financeAdmin");
    }

    public Object getAllAttributes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
        return defaultOidcUser.getClaims();
    }
}
