package com.justjava.ams.core.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import javax.ws.rs.ForbiddenException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KeycloakAdminService {

    @Autowired
    @Qualifier("adminKeycloak")
    private Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Helper method to provide detailed error messages for 403 Forbidden exceptions
     */
    private void handleKeycloakException(String operation, Exception e) {
        if (e instanceof ForbiddenException) {
            log.error("FORBIDDEN (HTTP 403): {}. This usually means the admin-cli-client service account lacks required realm-management roles. " +
                    "In Keycloak admin console: Realm: {} -> Clients -> admin-cli-client -> Service Account Roles -> " +
                    "Assign roles from realm-management client (manage-users, view-users, manage-realm, view-realm, etc.)", 
                    operation, realm, e);
        } else {
            log.error("Error during {}", operation, e);
        }
    }

    /**
     * Get all users from the connected realm
     */
    public List<UserRepresentation> getAllUsers(String search, PageRequest pageRequest) {
        try {
            if (search != null && !search.trim().isEmpty()) {
                return keycloakAdmin.realm(realm).users().search(search, pageRequest.getPageNumber(), pageRequest.getPageSize());
            }
            return keycloakAdmin.realm(realm).users().list(pageRequest.getPageNumber(), pageRequest.getPageSize());
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get total user count
     */
    public int getTotalUserCount() {
        try {
            return keycloakAdmin.realm(realm).users().count();
        } catch (Exception e) {
            log.error("Error getting user count", e);
            return 0;
        }
    }

    /**
     * Get active user count (enabled users)
     */
    public int getActiveUserCount() {
        try {
            List<UserRepresentation> users = keycloakAdmin.realm(realm).users().list();
            return (int) users.stream().filter(UserRepresentation::isEnabled).count();
        } catch (Exception e) {
            log.error("Error getting active user count", e);
            return 0;
        }
    }

    /**
     * Create a new user
     */
    public void createUser(String firstName, String lastName, String username, String email, String password, String realm) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setEmail(email);
            user.setEnabled(true);

            var response = keycloakAdmin.realm(realm).users().create(user);
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // Set password
            org.keycloak.representations.idm.CredentialRepresentation credential = new org.keycloak.representations.idm.CredentialRepresentation();
            credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            keycloakAdmin.realm(realm).users().get(userId).resetPassword(credential);

            log.info("User created successfully: {}", username);
        } catch (Exception e) {
            log.error("Error creating user", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Enable a user
     */
    public void enableUser(String userId) {
        try {
            UserRepresentation user = keycloakAdmin.realm(realm).users().get(userId).toRepresentation();
            user.setEnabled(true);
            keycloakAdmin.realm(realm).users().get(userId).update(user);
            log.info("User enabled: {}", userId);
        } catch (Exception e) {
            log.error("Error enabling user", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Disable a user
     */
    public void disableUser(String userId) {
        try {
            UserRepresentation user = keycloakAdmin.realm(realm).users().get(userId).toRepresentation();
            user.setEnabled(false);
            keycloakAdmin.realm(realm).users().get(userId).update(user);
            log.info("User disabled: {}", userId);
        } catch (Exception e) {
            log.error("Error disabling user", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String userId) {
        try {
            keycloakAdmin.realm(realm).users().get(userId).executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));
            log.info("Password reset email sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending password reset email", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete a user
     */
    public void deleteUser(String userId) {
        try {
            keycloakAdmin.realm(realm).users().delete(userId);
            log.info("User deleted: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting user", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Add user to group
     */
    public void addUserToGroup(String userId, String groupName) {
        try {
            List<GroupRepresentation> groups = keycloakAdmin.realm(realm).groups().groups(groupName, 0, 1);
            if (!groups.isEmpty()) {
                keycloakAdmin.realm(realm).users().get(userId).joinGroup(groups.get(0).getId());
                log.info("User {} added to group {}", userId, groupName);
            }
        } catch (Exception e) {
            log.error("Error adding user to group", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove user from group
     */
    public void removeUserFromGroup(String userId, String groupName) {
        try {
            List<GroupRepresentation> groups = keycloakAdmin.realm(realm).groups().groups(groupName, 0, 1);
            if (!groups.isEmpty()) {
                keycloakAdmin.realm(realm).users().get(userId).leaveGroup(groups.get(0).getId());
                log.info("User {} removed from group {}", userId, groupName);
            }
        } catch (Exception e) {
            log.error("Error removing user from group", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a group
     */
    public void createGroup(String groupName, String parentGroupId) {
        try {
            GroupRepresentation group = new GroupRepresentation();
            group.setName(groupName);

            keycloakAdmin.realm(realm).groups().add(group);
            log.info("Group created: {}", groupName);
        } catch (Exception e) {
            log.error("Error creating group", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all groups in the realm
     */
    public List<GroupRepresentation> getAllGroupsForRealm() {
        try {
            return keycloakAdmin.realm(realm).groups().groups();
        } catch (Exception e) {
            log.error("Error fetching groups", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get user groups
     */
    public List<String> getUserGroups(String userId) {
        try {
            return keycloakAdmin.realm(realm).users().get(userId).groups()
                    .stream()
                    .map(GroupRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            handleKeycloakException("fetching user groups", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get realm name
     */
    public String getRealmName() {
        return realm;
    }

    /**
     * Get current authenticated user name
     */
    public String getCurrentUserName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof DefaultOidcUser oidcUser) {
                String givenName = (String) oidcUser.getClaims().get("given_name");
                String familyName = (String) oidcUser.getClaims().get("family_name");
                return (givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "");
            }
        } catch (Exception e) {
            log.error("Error getting current user name", e);
        }
        return "Administrator";
    }

}
