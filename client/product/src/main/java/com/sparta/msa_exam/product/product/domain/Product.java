package com.sparta.msa_exam.product.product.domain;

import com.sparta.msa_exam.product.product.dto.ProductRequestDto;
import com.sparta.msa_exam.product.product.dto.ProductResponseDto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long product_id;
    private String name;
    private Integer supply_price;
    private Integer quantity;
    private String createdBy;


    public static Product createProduct(ProductRequestDto requestDto, String username) {
        return Product.builder()
                .name(requestDto.getName())
                .supply_price(requestDto.getSupply_price())
                .quantity(requestDto.getQuantity())
                .createdBy(username)
                .build();
    }

    public ProductResponseDto toResponseDto() {
        return new ProductResponseDto(
                this.product_id,
                this.name,
                this.supply_price,
                this.quantity,
                this.createdBy
        );
    }

    public void reduceQuantity(int i) {
        this.quantity = this.quantity - i;
    }

}
