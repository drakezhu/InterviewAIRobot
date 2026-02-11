package com.example.aiagent.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();

        // 强制设置请求编码为 UTF-8
        if (httpRequest.getCharacterEncoding() == null) {
            request.setCharacterEncoding("UTF-8");
        }

        // 设置响应编码
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        chain.doFilter(request, response);
    }
}