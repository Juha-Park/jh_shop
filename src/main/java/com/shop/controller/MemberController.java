package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.entyty.Member;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.validation.BindingResult;
import javax.validation.Valid;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping(value = "/new")
    public String memberForm(Model model){
        //model 객체를 이용하여 뷰에 전달할 데이터를 key, value 구조로 넣어줌.
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm"; //templates 폴더를 기준으로 실행할 뷰의 위치와 이름(memberForm)을 반환.
    }

    // 검증하려는 객체의 앞에 @Valid 어노테이션을 선언하고, 파라미터로 bindingResult 객체를 추가.
    // 검사 후 결과는 bindingResult에 담아줌.
    @PostMapping(value = "/new")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model){

        // bindingResult.hasError()를 호출하여 에러가 있다면 회원 가입 페이지로 이동.
        if(bindingResult.hasErrors()){
            return "member/memberForm";
        }

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch(IllegalStateException e){
            // 회원가입시 중복 회원 가입 예외가 발생하면 에러 메시지를 뷰로 전달.
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }

    @GetMapping(value = "/login")
    public String loginMember(){

        return "member/memberLoginForm";
    }

    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해 주세요.");
        return "member/memberLoginForm";
    }
}
