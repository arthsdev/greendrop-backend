package br.com.greendrop.backend.domain.service.product.specification;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public final class ProductSpecification {

    private ProductSpecification() {}

    // =============================
    // SIMPLE EQUAL FILTERS
    // =============================

    public static Specification<Product> hasStatus(ProductStatus status) {
        return equalsIfNotNull("status", status);
    }

    public static Specification<Product> hasCategory(ProductCategory category) {
        return equalsIfNotNull("category", category);
    }

    public static Specification<Product> postedBy(UUID userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("postedBy").get("id"), userId);
    }

    // =============================
    // JOIN FILTERS (NULL-SAFE)
    // =============================

    public static Specification<Product> hasRouteStop(UUID routeStopId) {
        return (root, query, cb) -> {
            if (routeStopId == null) return null;

            // Left join prevents NullPointer and does not force inner join
            return cb.equal(
                    root.join("routeStop", JoinType.LEFT).get("id"),
                    routeStopId
            );
        };
    }

    // =============================
    // RANGE FILTERS
    // =============================

    public static Specification<Product> hasWeightBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;

            Path<Double> weight = root.get("weightKg");

            if (min != null && max != null)
                return cb.between(weight, min, max);

            if (min != null)
                return cb.greaterThanOrEqualTo(weight, min);

            return cb.lessThanOrEqualTo(weight, max);
        };
    }

    public static Specification<Product> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;

            Path<LocalDateTime> createdAt = root.get("createdAt");

            if (from != null && to != null)
                return cb.between(createdAt, from, to);

            if (from != null)
                return cb.greaterThanOrEqualTo(createdAt, from);

            return cb.lessThanOrEqualTo(createdAt, to);
        };
    }

    // =============================
    // TEXT SEARCH
    // =============================

    public static Specification<Product> nameContains(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) return null;

            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + text.toLowerCase() + "%"
            );
        };
    }

    // =============================
    // INTERNAL UTILITIES
    // =============================

    private static <T> Specification<Product> equalsIfNotNull(String field, T value) {
        return (root, query, cb) ->
                value == null ? null : cb.equal(root.get(field), value);
    }
}