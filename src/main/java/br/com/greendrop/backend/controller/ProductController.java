package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.model.enums.ProductCategory;
import br.com.greendrop.backend.domain.model.enums.ProductStatus;
import br.com.greendrop.backend.domain.service.product.ProductService;
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

@Tag(name = "Products", description = "Endpoints for product management and search")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // =============================================================
    // CREATE PRODUCT
    // =============================================================
    @Operation(
            summary = "Create a new product",
            description = """
                    Creates a product.
                    Allowed roles: USER, ADMIN.
                    Forbidden role: COLLECTOR.
                    """,
            responses = {

                    @ApiResponse(
                            responseCode = "201",
                            description = "Product created successfully",
                            content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
                    ),

                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    ),

                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    ),

                    @ApiResponse(
                            responseCode = "403",
                            description = "Collector user is forbidden to create products",
                            content = @Content(schema = @Schema(implementation = ApiError.class),
                                    examples = @ExampleObject(
                                            name = "ProductForbidden",
                                            value = """
                                            {
                                              "status": 403,
                                              "code": "PRODUCT_FORBIDDEN",
                                              "message": "Collectors are not allowed to create products.",
                                              "path": "/api/products",
                                              "timestamp": "2025-01-01T12:00:00"
                                            }
                                            """
                                    )
                            )
                    ),

                    @ApiResponse(
                            responseCode = "404",
                            description = "Category or related resource not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    ),

                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ApiError.class))
                    )
            }
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductResponseDTO> create(
            @Valid @RequestBody ProductCreateDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(dto));
    }

    // =============================================================
    // GET PRODUCT BY ID
    // =============================================================
    @Operation(
            summary = "Get product by ID",
            responses = {

                    @ApiResponse(
                            responseCode = "200",
                            description = "Product found",
                            content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
                    ),

                    @ApiResponse(
                            responseCode = "404",
                            description = "Product not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class),
                                    examples = @ExampleObject(
                                            name = "NotFoundExample",
                                            value = """
                                            {
                                              "status": 404,
                                              "code": "PRODUCT_NOT_FOUND",
                                              "message": "Product not found.",
                                              "path": "/api/products/{id}",
                                              "timestamp": "2025-01-01T12:00:00"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // =============================================================
    // LIST PRODUCTS
    // =============================================================
    @Operation(
            summary = "List products with filters and pagination",
            description = "Allows filtering by status, category, weight, date range, and more.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products retrieved")
            }
    )
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
                        status, category, postedBy, routeStopId,
                        weightMin, weightMax, createdFrom, createdTo,
                        pageable
                )
        );
    }

    // =============================================================
    // LIST BY USER
    // =============================================================
    @Operation(
            summary = "List products by user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User products retrieved")
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProductResponseDTO>> byUser(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(productService.getProductsByUser(userId));
    }


    // =============================================================
    // LIST CREATED PRODUCTS
    // =============================================================
    @Operation(
            summary = "List products created by the authenticated user",
            description = """
                Returns all products created by the currently authenticated user.
                The user ID is extracted from the JWT token and automatically used to filter the results.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Products successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductResponseDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – missing or invalid authentication token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                {
                                  "timestamp": "2025-01-01T12:00:00",
                                  "status": 401,
                                  "error": "Unauthorized",
                                  "message": "Invalid or missing JWT token",
                                  "errorCode": "AUTH_401",
                                  "path": "/api/products/me"
                                }
                                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                {
                                  "timestamp": "2025-01-01T12:00:00",
                                  "status": 500,
                                  "error": "Internal Server Error",
                                  "message": "Unexpected server error",
                                  "errorCode": "SERVER_500",
                                  "path": "/api/products/me"
                                }
                                """)
                    )
            )
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductResponseDTO>> getMyProducts() {
        return ResponseEntity.ok(productService.getMyProducts());
    }

    // =============================================================
    // UPDATE PRODUCT
    // =============================================================
    @Operation(
            summary = "Update product",
            description = "Only product owner or admin may update.",
            responses = {

                    @ApiResponse(responseCode = "200", description = "Product updated"),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Product cannot be updated",
                            content = @Content(schema = @Schema(implementation = ApiError.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "status": 409,
                                              "code": "PRODUCT_CANNOT_UPDATE",
                                              "message": "This product is linked to a routeStop and cannot be modified.",
                                              "path": "/api/products/{id}",
                                              "timestamp": "2025-01-01T12:00:00"
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateDTO dto
    ) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    // =============================================================
    // DELETE PRODUCT
    // =============================================================
    @Operation(
            summary = "Soft delete a product",
            description = "Only owner or admin may delete.",
            responses = {

                    @ApiResponse(responseCode = "204", description = "Product deleted"),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Product cannot be deleted",
                            content = @Content(schema = @Schema(implementation = ApiError.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "status": 409,
                                              "code": "PRODUCT_CANNOT_DELETE",
                                              "message": "Product linked to routeStop cannot be deleted.",
                                              "path": "/api/products/{id}",
                                              "timestamp": "2025-01-01T12:00:00"
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        productService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}