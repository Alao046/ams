package com.justjava.ams.admin;

import com.justjava.ams.core.keycloak.KeycloakAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @GetMapping("/users")
    public String users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        try {
            // Get users from Keycloak
            var users = keycloakAdminService.getAllUsers(search, PageRequest.of(page, size));
            var groups = keycloakAdminService.getAllGroupsForRealm();
            var realm = keycloakAdminService.getRealmName();

            // Build userId -> group names map for the current page of users
            Map<String, List<String>> userGroupsMap = users.stream()
                    .collect(Collectors.toMap(
                            u -> u.getId(),
                            u -> {
                                try {
                                    return keycloakAdminService.getUserGroups(u.getId());
                                } catch (Exception e) {
                                    return Collections.emptyList();
                                }
                            }
                    ));

            model.addAttribute("users", users);
            model.addAttribute("groups", groups);
            model.addAttribute("userGroupsMap", userGroupsMap);
            model.addAttribute("realmName", realm);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("search", search);
            model.addAttribute("totalUsers", keycloakAdminService.getTotalUserCount());
            model.addAttribute("totalActive", keycloakAdminService.getActiveUserCount());
            model.addAttribute("totalPages", (keycloakAdminService.getTotalUserCount() + size - 1) / size);
            model.addAttribute("fromIndex", page * size + 1);
            model.addAttribute("toIndex", Math.min((page + 1) * size, keycloakAdminService.getTotalUserCount()));

            // Get current user info
            var currentUser = keycloakAdminService.getCurrentUserName();
            model.addAttribute("userName", currentUser);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to load users: " + e.getMessage());
        }

        return "admin/userManagement";
    }

    @PostMapping("/create-user")
    public String createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String realm,
            RedirectAttributes redirectAttributes) {

        try {
            keycloakAdminService.createUser(firstName, lastName, username, email, password, realm);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + username + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/enable-user")
    public String enableUser(@RequestParam String userId, RedirectAttributes redirectAttributes) {
        try {
            keycloakAdminService.enableUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User enabled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to enable user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/disable-user")
    public String disableUser(@RequestParam String userId, RedirectAttributes redirectAttributes) {
        try {
            keycloakAdminService.disableUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User disabled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to disable user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/reset-password-email")
    public String resetPasswordEmail(@RequestParam String userId, RedirectAttributes redirectAttributes) {
        try {
            keycloakAdminService.sendPasswordResetEmail(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Password reset email sent!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send reset email: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam String userId, RedirectAttributes redirectAttributes) {
        try {
            keycloakAdminService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/add/group")
    public String addGroupToUser(@RequestParam String userId, @RequestParam String groupName, RedirectAttributes redirectAttributes) {
        try {
            keycloakAdminService.addUserToGroup(userId, groupName);
            redirectAttributes.addFlashAttribute("successMessage", "User added to group '" + groupName + "'!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add user to group: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/remove/group")
    public String removeGroupFromUser(@RequestParam String userId, @RequestParam String groupName, RedirectAttributes redirectAttributes) {
        try {
            keycloakAdminService.removeUserFromGroup(userId, groupName);
            redirectAttributes.addFlashAttribute("successMessage", "User removed from group '" + groupName + "'!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove user from group: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/create-group")
    public String createGroup(@RequestParam String groupName, @RequestParam(required = false) String parentGroupId, RedirectAttributes redirectAttributes) {
        try {
            keycloakAdminService.createGroup(groupName, parentGroupId);
            redirectAttributes.addFlashAttribute("successMessage", "Group '" + groupName + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create group: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/user/{userId}/groups")
    @ResponseBody
    public List<String> getUserGroups(@PathVariable String userId) {
        try {
            return keycloakAdminService.getUserGroups(userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}