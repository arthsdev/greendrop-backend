package br.com.greendrop.backend.mapper.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.ProductImage;
import br.com.greendrop.backend.dto.product.ProductCreateDTO;
import br.com.greendrop.backend.dto.product.ProductDetailResponseDTO;
import br.com.greendrop.backend.dto.product.ProductListResponseDTO;
import br.com.greendrop.backend.dto.product.ProductUpdateDTO;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

/**
 * Pure mapping definition for Product transformations.
 * No business logic should live here.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    // =========================
    // DTO → Entity
    // =========================

    Product toEntity(ProductCreateDTO dto);

    void updateEntityFromDTO(ProductUpdateDTO dto, @MappingTarget Product entity);

    // =========================
    // Entity → LIST response
    // =========================

    @Mapping(target = "postedBy", source = "postedBy.id")
    ProductListResponseDTO toListResponse(Product product);

    // =========================
    // Entity → DETAIL response
    // =========================

    @Mapping(target = "postedBy", source = "postedBy.id")
    @Mapping(target = "claimedBy", source = "claimedBy.id")
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    ProductDetailResponseDTO toDetailResponse(Product product);

    // =========================
    // Helpers
    // =========================

    @Named("mapImages")
    static List<String> mapImages(List<ProductImage> images) {
        return images == null
                ? List.of()
                : images.stream()
                .map(ProductImage::getUrl)
                .toList();
    }
}
