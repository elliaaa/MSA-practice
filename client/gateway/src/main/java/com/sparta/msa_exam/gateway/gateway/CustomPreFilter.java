package com.sparta.msa_exam.gateway.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@Component
public class CustomPreFilter implements GlobalFilter, Ordered { //GlobalFilter: 전역 필터를 정의할 때 사용(모든 요청에 대해 필터가 동작)
                                                                //Ordered: 필터의 우선순위를 설정할 때 사용

    private static final Logger logger = Logger.getLogger(CustomPreFilter.class.getName());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 서버에 들어온 요청들을 기록해두는 역할을 하며, 문제를 찾거나 서버의 상태를 모니터링하는 데 유용
        ServerHttpRequest request = exchange.getRequest();
        logger.info("Pre Filter: Request URI is " + request.getURI());

        return chain.filter(exchange); //요청을 처리하는 후속 필터로 이동
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
