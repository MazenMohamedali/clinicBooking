package com.clinicHelper.user;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> deletSelf(Authentication authentication) {
        String email = authentication.getName();
        userService.deleteByUserEmail(email);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id, Authentication authentication) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
