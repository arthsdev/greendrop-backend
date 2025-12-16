package br.com.greendrop.backend.domain.service.route.authorization;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.exception.auth.ForbiddenException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.infrastructure.security.service.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class RouteAuthorizationService {

    private final CurrentUserService currentUserService;

    public RouteAuthorizationService(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public void requireAuthenticated() {
        UUID current = currentUserService.getCurrentUserId();
        if (current == null) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void requireAdmin() {
        User user = currentUserService.getCurrentUser();
        if (!user.getRole().name().equals("ADMIN")) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void requireAdminOrCollector(Route route) {
        requireAuthenticated();
        User user = currentUserService.getCurrentUser();
        if (user.getRole().name().equals("ADMIN")) return;

        UUID current = currentUserService.getCurrentUserId();
        if (!Objects.equals(current, route.getCollectorId())) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void authorizeRead(Route route) {
        User user = currentUserService.getCurrentUser();
        if (user.getRole().name().equals("ADMIN")) return;

        UUID current = currentUserService.getCurrentUserId();
        if (!Objects.equals(current, route.getCollectorId())) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void authorizeWrite(Route route) {
        User user = currentUserService.getCurrentUser();
        if (user.getRole().name().equals("ADMIN")) return;

        UUID current = currentUserService.getCurrentUserId();
        if (!Objects.equals(current, route.getCollectorId())) {
            throw new ForbiddenException(ErrorCode.UNAUTHORIZED);
        }
    }
}