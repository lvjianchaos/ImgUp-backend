package com.chaos.imgup.config;

import com.chaos.imgup.service.impl.UserDetailsServiceImpl;
import com.chaos.imgup.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. 检查 Header 或 Token 是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 如果没有Token，直接放行到下一个过滤器
            return;
        }

        // 2. 提取 Token
        jwt = authHeader.substring(7);

        // 3. 从 Token 中解析用户名
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // 如果Token解析失败（例如过期或无效），直接放行，后续的安全机制会处理
            filterChain.doFilter(request, response);
            return;
        }


        // 4. 验证 Token
        // 检查用户名不为空，并且当前安全上下文中没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从数据库加载用户信息
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 验证Token是否有效
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 创建一个认证令牌
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // 我们不需要凭证
                        userDetails.getAuthorities()
                );
                // 设置认证细节
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 将认证信息存入 Spring Security 的安全上下文
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 5. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}