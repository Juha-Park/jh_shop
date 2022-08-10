package com.shop.service;

import com.shop.dto.MemberFormDto;
import com.shop.entyty.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
//@Transactional 어노테이션을 선언하면 테스트 실행 후 롤백 처리가 되므로, 같은 메소드를 반복적으로 테스트할 수 있음.
@Transactional
// 테스트 코드 실행 시 application.properties보다 application-test.properties에 더 높은 우선순위를 부여.
// H2데이터베이스로 방언을 설정했으므로 테스트 코드 실행 시 기존 사용한 MySQL 대신 H2데이터베이스 사용.
@TestPropertySource(locations="classpath:application-test.properties")
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Member createMember(){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail("tiger@email.com");
        memberFormDto.setName("호랑이");
        memberFormDto.setAddress("백두산");
        memberFormDto.setPassword("1234");
        return Member.createMember(memberFormDto, passwordEncoder);
    }

    @Test
    @DisplayName("회원가입 테스트")
    public void saveMemberTest(){
        Member member = createMember();
        Member savedMember = memberService.saveMember(member);

        //저장하려고 요청했던 값과 실제 저장된 데이터를 비교.(첫 번째 파라미터는 기대값, 두 번째 파라미터는 실제로 저장된 값)
        assertEquals(member.getEmail(), savedMember.getEmail());
        assertEquals(member.getName(), savedMember.getName());
        assertEquals(member.getAddress(), savedMember.getAddress());
        assertEquals(member.getPassword(), savedMember.getPassword());
        assertEquals(member.getRole(), savedMember.getRole());
    }

    @Test
    @DisplayName("중복 회원 가입 테스트")
    public void saveDuplicateMemberTest(){
        Member member1 = createMember();
        Member member2 = createMember();
        memberService.saveMember(member1);

        Throwable e = assertThrows(IllegalStateException.class, () -> {memberService.saveMember(member2);});

        assertEquals("이미 가입된 회원입니다.", e.getMessage());
    }
}
