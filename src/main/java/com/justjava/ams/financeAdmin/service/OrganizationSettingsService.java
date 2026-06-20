package com.justjava.ams.financeAdmin.service;

import com.justjava.ams.financeAdmin.dto.OrganizationSettingsDTO;
import com.justjava.ams.financeAdmin.entity.OrganizationSettings;
import com.justjava.ams.financeAdmin.repository.OrganizationSettingsRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationSettingsService {

    private final OrganizationSettingsRepository organizationSettingsRepository;
    private final OrganizationRepository organizationRepository;

    public OrganizationSettingsDTO createOrganizationSettings(Long organizationId, OrganizationSettingsDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        OrganizationSettings settings = OrganizationSettings.builder()
                .organization(organization)
                .companyLogo(dto.getCompanyLogo())
                .companyWebsite(dto.getCompanyWebsite())
                .enableTwoFactorAuth(dto.getEnableTwoFactorAuth() != null ? dto.getEnableTwoFactorAuth() : true)
                .enablePasswordExpiry(dto.getEnablePasswordExpiry() != null ? dto.getEnablePasswordExpiry() : true)
                .passwordExpiryDays(dto.getPasswordExpiryDays() != null ? dto.getPasswordExpiryDays() : 90)
                .enableIPWhitelist(dto.getEnableIPWhitelist() != null ? dto.getEnableIPWhitelist() : false)
                .ipWhitelist(dto.getIpWhitelist())
                .enableSessionTimeout(dto.getEnableSessionTimeout() != null ? dto.getEnableSessionTimeout() : true)
                .sessionTimeoutMinutes(dto.getSessionTimeoutMinutes() != null ? dto.getSessionTimeoutMinutes() : 30)
                .dateFormat(dto.getDateFormat() != null ? dto.getDateFormat() : "MM/dd/yyyy")
                .timeFormat(dto.getTimeFormat() != null ? dto.getTimeFormat() : "HH:mm:ss")
                .timezone(dto.getTimezone() != null ? dto.getTimezone() : "UTC")
                .locale(dto.getLocale() != null ? dto.getLocale() : "en_US")
                .notes(dto.getNotes())
                .build();

        return mapToDTO(organizationSettingsRepository.save(settings));
    }

    public OrganizationSettingsDTO getOrganizationSettings(Long settingsId) {
        OrganizationSettings settings = organizationSettingsRepository.findById(settingsId)
                .orElseThrow(() -> new RuntimeException("Organization settings not found"));
        return mapToDTO(settings);
    }

    public OrganizationSettingsDTO getByOrganization(Long organizationId) {
        OrganizationSettings settings = organizationSettingsRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new RuntimeException("Settings not found for organization"));
        return mapToDTO(settings);
    }

    public OrganizationSettingsDTO updateOrganizationSettings(Long settingsId, OrganizationSettingsDTO dto) {
        OrganizationSettings settings = organizationSettingsRepository.findById(settingsId)
                .orElseThrow(() -> new RuntimeException("Organization settings not found"));

        if (dto.getCompanyLogo() != null) settings.setCompanyLogo(dto.getCompanyLogo());
        if (dto.getCompanyWebsite() != null) settings.setCompanyWebsite(dto.getCompanyWebsite());
        if (dto.getEnableTwoFactorAuth() != null) settings.setEnableTwoFactorAuth(dto.getEnableTwoFactorAuth());
        if (dto.getEnablePasswordExpiry() != null) settings.setEnablePasswordExpiry(dto.getEnablePasswordExpiry());
        if (dto.getPasswordExpiryDays() != null) settings.setPasswordExpiryDays(dto.getPasswordExpiryDays());
        if (dto.getEnableIPWhitelist() != null) settings.setEnableIPWhitelist(dto.getEnableIPWhitelist());
        if (dto.getIpWhitelist() != null) settings.setIpWhitelist(dto.getIpWhitelist());
        if (dto.getEnableSessionTimeout() != null) settings.setEnableSessionTimeout(dto.getEnableSessionTimeout());
        if (dto.getSessionTimeoutMinutes() != null) settings.setSessionTimeoutMinutes(dto.getSessionTimeoutMinutes());
        if (dto.getDateFormat() != null) settings.setDateFormat(dto.getDateFormat());
        if (dto.getTimeFormat() != null) settings.setTimeFormat(dto.getTimeFormat());
        if (dto.getTimezone() != null) settings.setTimezone(dto.getTimezone());
        if (dto.getLocale() != null) settings.setLocale(dto.getLocale());

        return mapToDTO(organizationSettingsRepository.save(settings));
    }

    private OrganizationSettingsDTO mapToDTO(OrganizationSettings settings) {
        return OrganizationSettingsDTO.builder()
                .id(settings.getId())
                .organizationId(settings.getOrganization().getId())
                .companyLogo(settings.getCompanyLogo())
                .companyWebsite(settings.getCompanyWebsite())
                .enableTwoFactorAuth(settings.getEnableTwoFactorAuth())
                .enablePasswordExpiry(settings.getEnablePasswordExpiry())
                .passwordExpiryDays(settings.getPasswordExpiryDays())
                .enableIPWhitelist(settings.getEnableIPWhitelist())
                .ipWhitelist(settings.getIpWhitelist())
                .enableSessionTimeout(settings.getEnableSessionTimeout())
                .sessionTimeoutMinutes(settings.getSessionTimeoutMinutes())
                .dateFormat(settings.getDateFormat())
                .timeFormat(settings.getTimeFormat())
                .timezone(settings.getTimezone())
                .locale(settings.getLocale())
                .notes(settings.getNotes())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
