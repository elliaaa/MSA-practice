package com.sparta.msa_exam.product.product.repository;

import com.sparta.msa_exam.product.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,ProductSearchRepository{
}
