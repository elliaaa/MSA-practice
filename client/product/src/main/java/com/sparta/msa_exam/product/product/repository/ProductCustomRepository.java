package com.sparta.msa_exam.product.product.repository;

import com.sparta.msa_exam.product.product.dto.ProductSearchDto;
import com.sparta.msa_exam.product.product.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCustomRepository {
    Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto, Pageable pageable);
}
