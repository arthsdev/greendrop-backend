package br.com.greendrop.backend.mapper.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.domain.model.ProductImage;
import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.dto.product.ProductCreateDTO;
import br.com.greendrop.backend.dto.product.ProductResponseDTO;
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
    // Entity → Response
    // =========================

    @Mapping(target = "postedBy", source = "postedBy", qualifiedByName = "mapUserToUUID")
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    ProductResponseDTO toResponse(Product entity);

    List<ProductResponseDTO> toResponseList(List<Product> products);

    // =========================
    // Custom mappers
    // =========================

    @Named("mapUserToUUID")
    static UUID mapUserToUUID(User user) {
        return user != null ? user.getId() : null;
    }

    @Named("mapImages")
    static List<String> mapImages(List<ProductImage> images) {
        return images == null
                ? List.of()
                : images.stream()
                .map(ProductImage::getUrl)
                .toList();
    }
}
