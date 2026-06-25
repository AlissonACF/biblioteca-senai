package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePopularSecurityContextQuandoTokenValido() throws Exception {
        JwtFilter filter = new JwtFilter(jwtUtil);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("user@teste.com");
        when(jwtUtil.extractRole("token")).thenReturn("PADRAO");

        filter.doFilterInternal(request, response, chain);

        assertEquals("user@teste.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(chain).doFilter(request, response);
    }
}
