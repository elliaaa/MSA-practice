package com.sparta.msa_exam.auth.auth;

import com.sparta.msa_exam.auth.auth.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
}
