package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.entyty.Member;
import com.shop.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

@SpringBootTest
//MockMvc 테스트를 하기 위해 선언.
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class MemberControllerTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    //MockMvc 클래스를 이용해 실제 객체와 비슷하지만 테스트에 필요한 기능만 가지는 가짜 객체를 생성.(웹 브라우저에서 요청하는 것처럼 테스트)
    private MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    //테스트 전 회원을 등록하는 메소드 생성.
    public Member createMember(String email, String password){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail(email);
        memberFormDto.setName("호랑이");
        memberFormDto.setAddress("백두산");
        memberFormDto.setPassword(password);
        Member member = Member.createMember(memberFormDto, passwordEncoder);
        return memberService.saveMember(member);
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    public void loginSuccessTest() throws Exception{
        String email = "test@email.com";
        String password = "1234";
        this.createMember(email, password);
        //userParameter()를 이용하여 이메일을 아이디로 세팅한 후 로그인 URL에 요청.
        mockMvc.perform(formLogin().userParameter("email")
                .loginProcessingUrl("/members/login") //회원가입 메소드 실행 후 가입된 회원 정보로 로그인이 되는지 테스트 진행.
                .user(email).password(password))
                .andExpect(SecurityMockMvcResultMatchers.authenticated()); //로그인이 성공하여 인증되면 테스트 코드 통과.
    }

    @Test
    @DisplayName("로그인 실패 테스트")
    public void loginFailTest() throws Exception {
        String email = "test@email.com";
        String password = "12345678";
        this.createMember(email, password);
        /*회원가입은 정상적으로 진행되었지만, 회원가입 시 입력한 비밀번호('12345678')가 아닌 다른 비밀번호('1234')로
          로그인을 시도하여 인증되지 않은 결과값이 출력되어 unauthenticated됨. */
        mockMvc.perform(formLogin().userParameter("email")
                .loginProcessingUrl("/members/login")
                .user(email).password("1234"))
                .andExpect(SecurityMockMvcResultMatchers.unauthenticated());
    }
}