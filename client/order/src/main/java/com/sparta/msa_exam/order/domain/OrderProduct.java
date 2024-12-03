package com.sparta.msa_exam.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "order_products")
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long order_product_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Long product_id;

    public OrderProduct(Long productId) {
        this.product_id = productId;
    }

    public OrderProduct(Long productId, Order order) {
        this.product_id = productId;
        this.order = order;
    }

    public Long getProductId() {
        return this.product_id;
    }
}
