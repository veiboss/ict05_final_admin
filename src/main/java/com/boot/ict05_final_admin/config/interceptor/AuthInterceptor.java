package com.boot.ict05_final_admin.config.interceptor;

import com.boot.ict05_final_admin.common.util.CookieUtil;
import com.boot.ict05_final_admin.config.HqProps;
import com.boot.ict05_final_admin.domain.auth.service.AuthGateway;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthGateway gateway;
    private final HqProps props;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String uri = req.getRequestURI(); // /admin/...
        // 정적/허용 경로는 통과 (WebMvcConfig에서 exclude하더라도 이중 안전)
        if (uri.startsWith(req.getContextPath()+"/login") ||
                uri.startsWith(req.getContextPath()+"/register") ||
                uri.startsWith(req.getContextPath()+"/assets") ||
                uri.startsWith(req.getContextPath()+"/css") ||
                uri.startsWith(req.getContextPath()+"/js")) return true;

        try { gateway.me(); return true; }
        catch (HttpClientErrorException.Unauthorized e) {
            String rt = CookieUtil.read(req, "refreshToken");
            if (rt != null) {
                try {
                    var t = gateway.refresh(rt);
                    CookieUtil.write(res,"accessToken",t.accessToken(),props.getCookieDomain(),props.isCookieSecure(),3600);
                    CookieUtil.write(res,"refreshToken",t.refreshToken(),props.getCookieDomain(),props.isCookieSecure(),60*60*24*14);
                    return true; // 재발급 성공, 진행
                } catch (Exception ignored) {}
            }
            CookieUtil.clear(res,"accessToken",props.getCookieDomain(),props.isCookieSecure());
            CookieUtil.clear(res,"refreshToken",props.getCookieDomain(),props.isCookieSecure());
            try {
                res.sendRedirect(req.getContextPath()+"/login");
            } catch (IOException ex) { throw new RuntimeException(ex); }
            return false;
        }
    }
}
