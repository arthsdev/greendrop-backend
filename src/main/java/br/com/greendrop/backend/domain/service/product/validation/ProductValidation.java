package br.com.greendrop.backend.domain.service.product.validation;

import br.com.greendrop.backend.exception.generic.BusinessException;
import br.com.greendrop.backend.exception.global.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ProductValidation
 *
 * <p>Responsible for input validation and integrity checks related to Product
 * domain operations.</p>
 *
 * <p>Main responsibilities:</p>
 * <ul>
 *     <li>Validate external URLs used as product images</li>
 *     <li>Ensure input consistency before business rules take place</li>
 *     <li>Prevent malformed data from being persisted</li>
 * </ul>
 *
 * <p>Security notes:</p>
 * <ul>
 *     <li>URL validation prevents JavaScript-injection or file:// exploits</li>
 *     <li>Apply stricter rules here if future image hosting is added</li>
 * </ul>
 */
@Component
public class ProductValidation {

    /**
     * Validates that all image URLs are non-null and use HTTP/HTTPS schemes.
     *
     * @param urls List of image URLs provided by the user
     * @throws BusinessException if any URL is invalid or unsafe
     */
    public void validateImageUrls(List<String> urls) {
        if (urls == null) return;

        for (String url : urls) {
            // Defensive validation: ensures only valid remote URLs are allowed
            if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
                throw new BusinessException(ErrorCode.PRODUCT_IMAGE_INVALID);
            }
        }
    }
}
