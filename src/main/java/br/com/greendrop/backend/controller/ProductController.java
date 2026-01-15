package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.service.product.ProductClaimService;
import br.com.greendrop.backend.domain.service.product.ProductService;
import br.com.greendrop.backend.domain.service.product.ProductUnclaimService;
import br.com.greendrop.backend.dto.product.*;
import br.com.greendrop.backend.exception.model.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Product management and lifecycle endpoints")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductClaimService productClaimService;
    private final ProductUnclaimService productUnclaimService;

    // =============================================================
    // CREATE
    // =============================================================
    @Operation(
            summary = "Create a product",
            description = """
                    Creates a new product.
                    
                    Allowed roles:
                    - USER
                    - ADMIN
                    
                    Forbidden:
                    - COLLECTOR
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Collector cannot create products",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductResponseDTO> create(
            @Valid @RequestBody ProductCreateDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.create(dto));
    }

    // =============================================================
    // GET BY ID
    // =============================================================
    @Operation(summary = "Get product by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // =============================================================
    // LIST
    // =============================================================
    @Operation(
            summary = "List products",
            description = "List products with filters and pagination"
    )
    @ApiResponse(responseCode = "200", description = "Products retrieved")
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> list(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) UUID postedBy,
            @RequestParam(required = false) UUID routeStopId,
            @RequestParam(required = false) Double weightMin,
            @RequestParam(required = false) Double weightMax,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdTo,

            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                productService.listProducts(
                        status,
                        category,
                        postedBy,
                        routeStopId,
                        weightMin,
                        weightMax,
                        createdFrom,
                        createdTo,
                        pageable
                )
        );
    }

    // =============================================================
    // MY PRODUCTS
    // =============================================================
    @Operation(summary = "List products created by authenticated user")
    @ApiResponse(
            responseCode = "200",
            description = "Products retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponseDTO.class)))
    )
    @GetMapping("/me")
    public ResponseEntity<List<ProductResponseDTO>> getMyProducts() {
        return ResponseEntity.ok(productService.getMyProducts());
    }

    // =============================================================
    // MY CLAIMS
    // =============================================================
    @Operation(
            summary = "List claimed products",
            description = "Returns products currently claimed by the authenticated collector"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Claimed products retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only collectors allowed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/me/claims")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ResponseEntity<List<ProductResponseDTO>> getMyClaimedProducts() {
        return ResponseEntity.ok(productService.getMyClaimedProducts());
    }

    // =============================================================
    // UPDATE
    // =============================================================
    @Operation(summary = "Update product")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Product cannot be updated",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateDTO dto
    ) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    // =============================================================
    // CLAIM
    // =============================================================
    @Operation(
            summary = "Claim product",
            description = """
                    Allows a collector to claim a product.
                    
                    Rules:
                    - Product must be PENDING
                    - Collector cannot be the creator
                    - Product cannot be linked to a route
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product claimed",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid product state",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/{id}/claim")
    public ResponseEntity<ProductResponseDTO> claim(@PathVariable UUID id) {
        return ResponseEntity.ok(productClaimService.claim(id));
    }

    // =============================================================
    // UNCLAIM
    // =============================================================
    @Operation(
            summary = "Unclaim product",
            description = """
                    Allows a collector to unclaim a product.
                    
                    Rules:
                    - Must be current collector
                    - Product must be ASSIGNED
                    - Product cannot be linked to a route
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product unclaimed",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid product state",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/{id}/unclaim")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ResponseEntity<ProductResponseDTO> unclaim(@PathVariable UUID id) {
        return ResponseEntity.ok(productUnclaimService.unclaim(id));
    }

    // =============================================================
    // DELETE
    // =============================================================
    @Operation(summary = "Soft delete product")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(
                    responseCode = "409",
                    description = "Product cannot be deleted",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
