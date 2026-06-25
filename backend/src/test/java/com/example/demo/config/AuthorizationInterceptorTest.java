package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import com.example.demo.controller.TesteSecurityController;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthorizationInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveLiberarErroSwaggerHandlerNaoMetodoEPublico() throws Exception {
        AuthorizationInterceptor interceptor = interceptor();
        when(request.getDispatcherType()).thenReturn(DispatcherType.ERROR);
        assertTrue(interceptor.preHandle(request, response, new Object()));

        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        assertTrue(interceptor.preHandle(request, response, new Object()));

        when(request.getRequestURI()).thenReturn("/qualquer");
        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertTrue(interceptor.preHandle(request, response, handler("rotaPublica")));
    }

    @Test
    void deveRecusarQuandoTokenAusenteOuInvalido() throws Exception {
        AuthorizationInterceptor interceptor = interceptor();
        HandlerMethod handler = handler("rotaAdmin");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        when(request.getRequestURI()).thenReturn("/test-security/admin");

        assertFalse(interceptor.preHandle(request, response, handler));
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void deveAutenticarAdminComTokenValido() throws Exception {
        AuthorizationInterceptor interceptor = interceptor();
        HandlerMethod handler = handler("rotaAdmin");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        when(request.getRequestURI()).thenReturn("/test-security/admin");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("admin@teste.com");
        when(jwtUtil.extractRole("token")).thenReturn("ADMIN");

        assertTrue(interceptor.preHandle(request, response, handler));
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void deveRecusarUsuarioPadraoEmRotaAdmin() throws Exception {
        AuthorizationInterceptor interceptor = interceptor();
        HandlerMethod handler = handler("rotaAdmin");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        when(request.getRequestURI()).thenReturn("/test-security/admin");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("user@teste.com");
        when(jwtUtil.extractRole("token")).thenReturn("PADRAO");

        assertFalse(interceptor.preHandle(request, response, handler));
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private AuthorizationInterceptor interceptor() {
        AuthorizationInterceptor interceptor = new AuthorizationInterceptor();
        ReflectionTestUtils.setField(interceptor, "jwtUtil", jwtUtil);
        return interceptor;
    }

    private HandlerMethod handler(String methodName) throws NoSuchMethodException {
        return new HandlerMethod(new TesteSecurityController(),
                TesteSecurityController.class.getMethod(methodName));
    }
}
