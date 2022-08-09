package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    MemberService memberService;

    //http 요청에 대한 보안 설정.
    //loginPage()로 로그인 페이지 url을 설정.
    //defaultSuccessUrl()로 로그인 성공 시 이동할 url을 설정.
    //usernameParameter()로 로그인 시 사용할 파라미터 이름을 'email'로 지정.
    //failureUrl()로 로그인 실패 시 이동할 url을 설정.
    //logoutRequestMatcher()로 로그아웃 url을 설정.
    //logoutSuccessUrl()로 로그아웃 성공 시 이동할 url을 설정.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.formLogin()
                .loginPage("/members/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/")
                .failureUrl("/members/login/error")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/");

        //security 처리에 HttpServletRequest를 이용.
        //permitAll()를 통해 모든 사용자가 로그인(인증) 없이 해당 경로에 접근할 수 있도록 설정.
        //해당 계정이 ADMIN ROLE일 경우에만 접근 가능하도록 설정.
        //위에서 설정한 경로를 제외한 나머지 경로들은 모두 로그인(인증)을 요구하도록 설정.
        http.authorizeRequests()
                .mvcMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                .mvcMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();

        //인증되지 않은 사용자가 리소스에 접근하였을 때 수행되는 핸들러를 등록.
        http.exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint());

        return http.build();
    }


    //BCryptPasswordEncoder의 해시 함수를 이용하여 비밀번호를 암호화하여 저장한 뒤 이를 Bean에 저장.
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    //static 디렉토리의 하위 파일은 인증을 무시하도록 설정.
    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring().mvcMatchers("/css/**", "/js/**", "/img/**");
    }

}