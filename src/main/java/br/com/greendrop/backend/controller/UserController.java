package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.UserService;
import br.com.greendrop.backend.dto.user.PasswordUpdateDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.dto.user.UserUpdateDTO;
import br.com.greendrop.backend.dto.pagination.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    // ============================================================
    // LIST ALL USERS (ADMIN)
    // ============================================================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (ADMIN)")
    public ResponseEntity<PaginatedResponse<UserResponseDTO>> listAll(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    // ============================================================
    // GET USER BY ID (ADMIN)
    // ============================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (ADMIN)")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ============================================================
    // GET LOGGED USER PROFILE
    // ============================================================
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','COLLECTOR','ADMIN')")
    @Operation(summary = "Get logged user profile")
    public ResponseEntity<UserResponseDTO> getMe() {
        return ResponseEntity.ok(userService.getLoggedUser());
    }

    // ============================================================
    // UPDATE LOGGED USER PROFILE
    // ============================================================
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER','COLLECTOR','ADMIN')")
    @Operation(summary = "Update logged user profile")
    public ResponseEntity<UserResponseDTO> updateMe(@Valid @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateUserProfile(dto));
    }

    // ============================================================
    // UPDATE PASSWORD
    // ============================================================
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','COLLECTOR','ADMIN')")
    @Operation(summary = "Update password for logged user")
    public void updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(dto.oldPassword(), dto.newPassword());
    }

    // ============================================================
    // DELETE USER (ADMIN)
    // ============================================================
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (ADMIN)")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
