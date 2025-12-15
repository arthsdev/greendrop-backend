package br.com.greendrop.backend.domain.service.authorization;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.service.AuthService;
import br.com.greendrop.backend.exception.auth.ForbiddenException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * RouteAuthorizationService — centralizes route-specific authorization checks.
 *
 * Delegates to AuthService for "who is the current user" information
 * so we have a single source of truth for the authenticated user.
 */
@Service
public class RouteAuthorizationService {

    private final AuthService authService;

    public RouteAuthorizationService(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Ensure the caller is authenticated.
     */
    public void requireAuthenticated() {
        UUID current = authService.getCurrentUserId();
        if (current == null) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * Ensure caller is admin.
     */
    public void requireAdmin() {
        if (!authService.isCurrentUserAdmin()) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * Ensure caller is admin or the collector assigned to the route.
     */
    public void requireAdminOrCollector(Route route) {
        requireAuthenticated();
        if (authService.isCurrentUserAdmin()) return;
        UUID current = authService.getCurrentUserId(); // note: replace - in code below correct call
        if (!Objects.equals(current, route.getCollectorId())) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * Ensure caller can read the route: admin or assigned collector.
     */
    public void authorizeRead(Route route) {
        if (authService.isCurrentUserAdmin()) return;
        UUID current = authService.getCurrentUserId();
        if (!Objects.equals(current, route.getCollectorId())) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * Ensure caller can write on route (change status / modify stops): admin or assigned collector.
     */
    public void authorizeWrite(Route route) {
        if (authService.isCurrentUserAdmin()) return;
        UUID current = authService.getCurrentUserId();
        if (!Objects.equals(current, route.getCollectorId())) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }
}
