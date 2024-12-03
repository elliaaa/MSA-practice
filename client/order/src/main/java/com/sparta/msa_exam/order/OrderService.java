package com.sparta.msa_exam.order;

import com.sparta.msa_exam.order.client.ProductClient;
import com.sparta.msa_exam.order.client.ProductResponseDto;
import com.sparta.msa_exam.order.domain.Order;
import com.sparta.msa_exam.order.domain.OrderProduct;
import com.sparta.msa_exam.order.dto.OrderRequestDto;
import com.sparta.msa_exam.order.dto.OrderResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    private final ProductClient productClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProduct")
    public ProductResponseDto getProduct(Long productId) {
        return productClient.getProduct(productId);
    }

    public ProductResponseDto fallbackGetProduct(Long productId, Throwable throwable) {
        // 실패 시 fallback 로직 실행
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "잠시 후에 주문 추가를 요청 해주세요.");
    }

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto, String userId) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. 상품 존재 여부 및 재고 확인
        for (Long productId : orderRequestDto.getOrder_products_id()) {
            ProductResponseDto product = productClient.getProduct(productId);
            if (product.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with ID " + productId + " is out of stock.");
            }
            totalAmount = totalAmount.add(BigDecimal.valueOf(product.getSupply_price()));
        }

        // 2. 상품 재고 감소 처리
        for (Long productId : orderRequestDto.getOrder_products_id()) {
            productClient.reduceProductQuantity(productId, 1);
        }

        Order order = Order.createOrder(orderRequestDto.getOrder_products_id(),orderRequestDto.getTotal_amount());
        Order savedOrder = orderRepository.save(order);

        return toResponseDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id " + orderId));

        return toResponseDto(order);
    }

    @Transactional
    public OrderResponseDto updateOrder(Long orderId, OrderRequestDto orderRequestDto, String userId) {
        // 1. 주문 존재 여부 확인
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id " + orderId));

        // 2. 상품 존재 여부 및 재고 확인
        for (Long productId : orderRequestDto.getOrder_products_id()) {
            ProductResponseDto product = productClient.getProduct(productId);
            if (product.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with ID " + productId + " is out of stock.");
            }
        }

        // 3. 상품 추가: OrderProduct로 매핑 후 order에 추가
        List<OrderProduct> newOrderProducts = orderRequestDto.getOrder_products_id().stream()
                .map(productId -> new OrderProduct(productId, order)) // productId와 order를 매핑
                .collect(Collectors.toList());

        order.getOrder_products_id().addAll(newOrderProducts); // 기존 주문에 새 상품 추가

       // 4. 총 금액(total_amount) 계산
        BigDecimal totalAmount = BigDecimal.ZERO;  // 총 금액 초기화
        for (Long productId : orderRequestDto.getOrder_products_id()) {
            ProductResponseDto product = productClient.getProduct(productId);
            totalAmount = totalAmount.add(BigDecimal.valueOf(product.getSupply_price()));
        }
        order.setTotalAmount(totalAmount);  // 주문의 총 금액을 업데이트

        // 5. 상품 재고 감소 처리 (추가된 상품에 대해서만)
        for (Long productId : orderRequestDto.getOrder_products_id()) {
            productClient.reduceProductQuantity(productId, 1);
        }
        Order updatedOrder = orderRepository.save(order);

        return toResponseDto(updatedOrder);
    }

    private OrderResponseDto toResponseDto(Order order) {
        // 주문에 포함된 모든 상품 ID를 가져와서 List<Long>으로 변환
        List<Long> productIds = order.getOrder_products_id().stream()
                .map(OrderProduct::getProductId)  // OrderProduct에서 product_id를 가져옴
                .collect(Collectors.toList());

        return new OrderResponseDto(order.getOrder_id(), productIds,order.getTotal_amount());
    }
}
