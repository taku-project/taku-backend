package com.ani.taku_backend.user.model.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.user.model.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
/**
 * 스프링 시큐리티에 저장할 유저 정보
 */
@ToString
@RequiredArgsConstructor
public class PrincipalUser implements UserDetails {

    private User user;

    public PrincipalUser(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public User getUser() {
        return user;
    }

    // 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        return authorities;
    }

    // 비밀번호 반환
    @Override
    public String getPassword() {
        return null;
    }

    // 이메일 반환
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 활성 여부 반환
    @Override
    public boolean isEnabled() {
        return StatusType.ACTIVE.name().equals(user.getStatus());
    }
}
