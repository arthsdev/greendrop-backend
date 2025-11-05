package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.UserService;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing users.
 * Exposes endpoints for registration and admin operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Public endpoint — register a new user.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody UserRequestDTO dto) {
        return userService.create(dto);
    }

    /**
     * Admin-only — get paginated list of all users.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponseDTO> listAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

    /**
     * Authenticated users — get their own profile.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'COLLECTOR', 'ADMIN')")
    public UserResponseDTO findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    /**
     * Admin-only — delete a user by ID.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
