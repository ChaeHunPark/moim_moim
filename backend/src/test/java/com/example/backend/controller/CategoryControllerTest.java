package com.example.backend.controller;


import com.example.backend.entity.Category;
import com.example.backend.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class) // Controller 레이어만 테스트
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    @WithMockUser // 🎯 가짜 사용자를 생성하여 Security 인증을 통과시킴
    @DisplayName("카테고리 전체 목록을 조회하면 HTTP 200 OK를 반환한다")
    void getCategoriesTest() throws Exception {
        // given: 테스트용 데이터 준비
        List<Category> categories = List.of(
                new Category("개발"),
                new Category("운동")
        );
        given(categoryRepository.findAll()).willReturn(categories);

        // when & then: 호출 및 검증
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk()) // 🎯 여기서 HTTP 200(OK) 확인!
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("개발"))
                .andDo(print());
    }
}