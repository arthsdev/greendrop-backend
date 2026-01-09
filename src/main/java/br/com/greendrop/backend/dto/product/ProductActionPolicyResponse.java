package br.com.greendrop.backend.dto.product;

public record ProductActionPolicyResponse(
        boolean canEdit,
        boolean canDelete,
        boolean canClaim,
        boolean canUnclaim
) {
}
