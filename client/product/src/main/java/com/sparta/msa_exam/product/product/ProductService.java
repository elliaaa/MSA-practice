package com.sparta.msa_exam.product.product;


import com.sparta.msa_exam.product.product.domain.Product;
import com.sparta.msa_exam.product.product.dto.ProductRequestDto;
import com.sparta.msa_exam.product.product.dto.ProductResponseDto;
import com.sparta.msa_exam.product.product.dto.ProductSearchDto;
import com.sparta.msa_exam.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, String userId) {
        Product product = Product.createProduct(requestDto, userId);
        Product savedProduct = productRepository.save(product);
        return toResponseDto(savedProduct);
    }

    public Page<ProductResponseDto> getProducts(ProductSearchDto searchDto, Pageable pageable) {
        return productRepository.searchProducts(searchDto, pageable);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or has been deleted"));
        return toResponseDto(product);
    }

    @Transactional
    public void reduceProductQuantity(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough quantity for product ID: " + productId);
        }

        product.reduceQuantity(quantity);
        productRepository.save(product);
    }

    private ProductResponseDto toResponseDto(Product product) {
        return new ProductResponseDto(
                product.getProduct_id(),
                product.getName(),
                product.getSupply_price(),
                product.getQuantity(),
                product.getCreatedBy()
        );
    }
}