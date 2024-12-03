package com.sparta.msa_exam.product.product;

import com.sparta.msa_exam.product.product.dto.ProductRequestDto;
import com.sparta.msa_exam.product.product.dto.ProductResponseDto;
import com.sparta.msa_exam.product.product.dto.ProductSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ProductResponseDto createProduct(@RequestBody ProductRequestDto productRequestDto,
                                            @RequestHeader(value = "X-User-Id", required = true) String userId,
                                            @RequestHeader(value = "X-Role", required = true) String role) {
        if (!"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. User role is not MANAGER.");
        }
        return productService.createProduct(productRequestDto, userId);
    }

    @GetMapping
    public Page<ProductResponseDto> getProducts(ProductSearchDto searchDto, Pageable pageable) { //전체목록조회
        return productService.getProducts(searchDto, pageable);
    }

    @GetMapping("/{productId}")
    public ProductResponseDto getProductById(@PathVariable Long productId) { // 한 상품 조회
        return productService.getProductById(productId);
    }

    @GetMapping("/{id}/reduceQuantity")
    public void reduceProductQuantity(@PathVariable Long id, @RequestParam int quantity) {
        productService.reduceProductQuantity(id, quantity);
    }
}
