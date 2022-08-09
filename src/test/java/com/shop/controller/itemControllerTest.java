package com.shop.controller;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class itemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("상품 등록 페이지 권한 테스트")
    //현재 인증된 사용자의 role을 ADMIN으로 셋팅.
    @WithMockUser(roles = "ADMIN")
    public void itemFormTest() throws Exception{
        // 상품 등록 페이지에 get 요청을 보냄.
        // print()로 요청과 응답 메시지를 확인할 수 있도록 콘솔창에 출력.
        // 응답 상태 코드가 정상인지 확인.
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/item/new"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 등록 페이지 일반 회원 접근 테스트")
    // 현재 인증된 사용자의 role을 USER로 셋팅.
    @WithMockUser(roles="USER")
    public void itemFormNotAdminTest() throws Exception{
        //상품 등록 페이지 진입 요청 시 Forbidden 예외가 발생하면 테스트 통과.
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/item/new"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
