package com.boot.ict05_final_admin.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// application-receive.properties
// sync.shared-secret=local-dev-secret
@Component
@Order(0)
public class SyncAuthFilter extends OncePerRequestFilter {

    @Value("${sync.shared-secret}")
    private String sharedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String path = req.getRequestURI();

        // 1) 프리플라이트는 통과
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // 2) 본사 동기화 API만 검사
        if (path.startsWith("/API/receive/")) {
            String token = req.getHeader("X-Sync-Auth");
            if (token == null || !token.equals(sharedSecret)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("Missing or invalid X-Sync-Auth");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}


