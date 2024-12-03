package com.sparta.msa_exam.product.product.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.msa_exam.product.product.domain.Product;
import com.sparta.msa_exam.product.product.domain.QProduct;
import com.sparta.msa_exam.product.product.dto.ProductResponseDto;
import com.sparta.msa_exam.product.product.dto.ProductSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sparta.msa_exam.product.product.domain.QProduct.product;


@RequiredArgsConstructor
public class ProductSearchImpl implements ProductSearchRepository {

    private final JPAQueryFactory queryFactory;
    private final QProduct product = QProduct.product;

    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto, Pageable pageable) {
        List<OrderSpecifier<?>> orders = getAllOrderSpecifiers(pageable);

        QueryResults<Product> results = queryFactory
                .selectFrom(product)
                .where(
                        nameContains(searchDto.getName()),
                        priceBetween(searchDto.getMinPrice(), searchDto.getMaxPrice()),
                        quantityBetween(searchDto.getMinQuantity(), searchDto.getMaxQuantity())
                )
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<ProductResponseDto> content = results.getResults().stream()
                .map(Product::toResponseDto)
                .collect(Collectors.toList());
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? product.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression priceBetween(Double minPrice, Double maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return product.supply_price.between(minPrice, maxPrice);
        } else if (minPrice != null) {
            return product.supply_price.goe(minPrice);
        } else if (maxPrice != null) {
            return product.supply_price.loe(maxPrice);
        } else {
            return null;
        }
    }

    private BooleanExpression quantityBetween(Integer minQuantity, Integer maxQuantity) {
        if (minQuantity != null && maxQuantity != null) {
            return product.quantity.between(minQuantity, maxQuantity);
        } else if (minQuantity != null) {
            return product.quantity.goe(minQuantity);
        } else if (maxQuantity != null) {
            return product.quantity.loe(maxQuantity);
        } else {
            return null;
        }
    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort() != null) { // 정렬 조건이 있으면 그에 맞는 정렬을 다음과 같이 처리
            for (Sort.Order sortOrder : pageable.getSort()) {
                com.querydsl.core.types.Order direction = sortOrder.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
                switch (sortOrder.getProperty()) {
                    case "supply_price":
                        orders.add(new OrderSpecifier<>(direction, product.supply_price));
                        break;
                    case "quantity":
                        orders.add(new OrderSpecifier<>(direction, product.quantity));
                        break;
                    default:
                        break;
                }
            }
        }
        return orders;
    }
}