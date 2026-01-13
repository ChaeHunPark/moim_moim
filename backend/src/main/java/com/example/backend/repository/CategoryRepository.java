package com.example.backend.repository;

import com.example.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 카테고리 이름으로 조회 (초기 데이터 확인용)
    Optional<Category> findByName(String name);
}
