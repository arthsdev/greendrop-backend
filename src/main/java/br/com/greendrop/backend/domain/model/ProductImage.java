package br.com.greendrop.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
/**
 * Represents a single image belonging to a product.
 */
@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String url;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;
}
