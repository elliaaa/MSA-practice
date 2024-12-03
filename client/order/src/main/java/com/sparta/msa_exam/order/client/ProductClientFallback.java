package com.sparta.msa_exam.order.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductClientFallback implements ProductClient{

    @Override
    public ProductResponseDto getProduct(Long id) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "잠시 후에 주문 추가를 요청 해주세요.");
    }

    @Override
    public void reduceProductQuantity(Long id, int quantity) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "잠시 후에 주문 추가를 요청 해주세요.");
    }
}
