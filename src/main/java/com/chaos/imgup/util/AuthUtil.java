package com.chaos.imgup.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chaos.imgup.entity.User;
import com.chaos.imgup.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    private static UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        AuthUtil.userMapper = userMapper;
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("用户未登录");
        }
        String username = authentication.getName();
        // 注意：这里为了获取完整的User对象（包含ID），从数据库查询。
        // 在高并发场景下，可考虑将用户信息缓存（如Redis）或直接存入JWT来优化。
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }
}