package com.sparta.msa_exam.gateway.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
@Slf4j
@Component
public class LocalJwtAuthenticationFilter implements GlobalFilter {

    @Value("${service.jwt.secret-key}")
    private String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {  // ServerWebExchange란? 웹 요청과 응답을 관리하는 컨테이너
        String path = exchange.getRequest().getURI().getPath();

        if (path.equals("/auth/signIn") || path.equals("/auth/signUp")) {  // /signIn, /signUp 경로는 필터를 적용하지 않음
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);

        if (token == null || !validateToken(token, exchange)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);  // 토큰이 유효하면 다음 필터로 요청을 전달
    }

    private String extractToken(ServerWebExchange exchange) { //Authorization 헤더에서 JWT 토큰을 뽑아옴
                                                              //Bearer로 시작하는 부분이 JWT 토큰이기 때문에, Bearer 뒤의 토큰을 추출
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token, ServerWebExchange exchange) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey)); //secretKey를 이용해 HMAC SHA 알고리즘으로 토큰을 해석

            // 토큰이 유효한지 판단
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build().parseSignedClaims(token);
            log.info("#####payload :: " + claimsJws.getPayload().toString());

            Claims claims = claimsJws.getBody();

            //유효한 토큰이라면 토큰에서 user_id와 role 정보를 추출하여 요청의 헤더에 넣음
            exchange.getRequest().mutate()
                    .header("X-User-Id", claims.get("user_id").toString())
                    .header("X-Role", claims.get("role").toString())
                    .build();

            // 추가적인 검증 로직 (예: 토큰 만료 여부 확인 등)을 여기에 추가할 수 있습니다.
            return true;
        } catch (Exception e) {
            return false;
        }
    }



}