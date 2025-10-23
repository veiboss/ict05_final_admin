//package com.boot.ict05_final_admin.config.interceptor;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.lang.Nullable;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
///**
// * - 로그인 사용자 정보를 매 요청의 모델에 넣어준다 (타임리프에서 사용 가능)
// * - 필요하다면 preHandle에서 추가 가드도 가능
// */
//public class LoginSessionInterceptor implements HandlerInterceptor {
//
//    // 컨트롤러 실행 전
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // Spring Security가 인증은 처리하므로 여기선 별도 차단 로직 불필요.
//        // 만약 비보호 URL에서도 인증이 꼭 필요하다면 여기서 redirect 가능.
//        return true;
//    }
//
//    // 컨트롤러 실행 후, 뷰 렌더링 전
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
//                           @Nullable ModelAndView modelAndView) throws Exception {
//        if (modelAndView == null) return;
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
//            // 로그인 사용자명을 모델에 추가 (원하면 UserDetails 캐스팅해서 더 넣을 수 있음)
//            modelAndView.addObject("loginUsername", auth.getName());
//        }
//    }
//}
