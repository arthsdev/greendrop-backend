package br.com.greendrop.backend.mapper.product;

import br.com.greendrop.backend.domain.model.Product;
import br.com.greendrop.backend.dto.product.ProductCreateDTO;
import br.com.greendrop.backend.dto.product.ProductResponseDTO;
import br.com.greendrop.backend.dto.product.ProductUpdateDTO;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Decorator that extends MapStruct-generated mapper functionality.
 *
 * This class should contain *business-specific* mapping rules,
 * without ever polluting the main mapper interface.
 */
public abstract class ProductMapperDecorator implements ProductMapper {

    protected ProductMapper delegate;

    @Autowired
    public void setDelegate(ProductMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public Product toEntity(ProductCreateDTO dto) {
        return delegate.toEntity(dto);
    }

    @Override
    public void updateEntityFromDTO(ProductUpdateDTO dto, @MappingTarget Product entity) {
        delegate.updateEntityFromDTO(dto, entity);
    }

    @Override
    public ProductResponseDTO toResponse(Product entity) {
        return delegate.toResponse(entity);
    }

    @Override
    public List<ProductResponseDTO> toResponseList(List<Product> products) {
        return delegate.toResponseList(products);
    }

    // TODO: add custom mapping logic here when needed
}
