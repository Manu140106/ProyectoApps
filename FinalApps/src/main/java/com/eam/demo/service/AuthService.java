package com.eam.demo.service;

import com.eam.demo.dto.AuthRequest;
import com.eam.demo.dto.AuthResponse;
import com.eam.demo.dto.OrganizationResponse;
import com.eam.demo.dto.RegisterOrganizationRequest;
import com.eam.demo.entity.Organization;
import com.eam.demo.entity.Role;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.exception.BadRequestException;
import com.eam.demo.repository.OrganizationRepository;
import com.eam.demo.repository.UserAccountRepository;
import com.eam.demo.security.CustomUserDetails;
import com.eam.demo.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TenantContextService tenantContextService;

    public AuthService(
            OrganizationRepository organizationRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TenantContextService tenantContextService
    ) {
        this.organizationRepository = organizationRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tenantContextService = tenantContextService;
    }

    @Transactional
    public OrganizationResponse registerOrganization(RegisterOrganizationRequest request) {
        if (organizationRepository.existsByNameIgnoreCase(request.getOrganizationName())) {
            throw new BadRequestException("Ya existe una organizacion con ese nombre");
        }

        if (organizationRepository.existsByCodeIgnoreCase(request.getOrganizationCode())) {
            throw new BadRequestException("Ya existe una organizacion con ese codigo");
        }

        if (userAccountRepository.existsByEmailIgnoreCase(request.getAdminEmail())) {
            throw new BadRequestException("Ya existe un usuario con ese email");
        }

        Organization organization = new Organization();
        organization.setName(request.getOrganizationName().trim());
        organization.setCode(request.getOrganizationCode().trim().toUpperCase());
        organization = organizationRepository.save(organization);

        UserAccount admin = new UserAccount();
        admin.setFullName(request.getAdminName().trim());
        admin.setEmail(request.getAdminEmail().trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        admin.setRole(Role.ADMIN);
        admin.setOrganization(organization);
        userAccountRepository.save(admin);

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getCode(),
                organization.isActive()
        );
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales invalidas"));

        if (!user.isActive()) {
            throw new BadRequestException("Usuario inactivo");
        }

        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getOrganization().getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.isActive()
        );

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getOrganization().getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public OrganizationResponse currentOrganization() {
        Organization organization = tenantContextService.currentOrganization();
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getCode(),
                organization.isActive()
        );
    }
}
