package com.eam.demo.service;

import com.eam.demo.dto.UserRequest;
import com.eam.demo.dto.UserResponse;
import com.eam.demo.entity.Organization;
import com.eam.demo.entity.Role;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.exception.BadRequestException;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantContextService tenantContextService;

    public UserService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            TenantContextService tenantContextService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantContextService = tenantContextService;
    }

    public List<UserResponse> listByCurrentOrganization() {
        Organization organization = tenantContextService.currentOrganization();
        return userAccountRepository.findAllByOrganizationOrderByCreatedAtDesc(organization)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        Organization organization = tenantContextService.currentOrganization();

        if (userAccountRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Ya existe un usuario con ese email");
        }

        if (request.getRole() == null) {
            request.setRole(Role.USER);
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("La clave es obligatoria para crear usuario");
        }

        UserAccount user = new UserAccount();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(request.getActive() == null || request.getActive());
        user.setOrganization(organization);

        return toResponse(userAccountRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        Organization organization = tenantContextService.currentOrganization();

        UserAccount user = userAccountRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setFullName(request.getFullName().trim());
        user.setRole(request.getRole());

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userAccountRepository.save(user));
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getOrganization().getId()
        );
    }
}
