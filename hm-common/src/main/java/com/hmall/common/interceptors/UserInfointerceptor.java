package com.hmall.common.interceptors;

import com.hmall.common.utils.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserInfointerceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取登录用户信息
        String userInfo = request.getHeader("user-info");
        //判断是否获取了用户，如果获取了，则存入ThreadLocal
        if(userInfo != null)
            // 存入ThreadLocal
            UserContext.setUser(Long.valueOf(userInfo));
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户信息
        UserContext.removeUser();
    }
}
