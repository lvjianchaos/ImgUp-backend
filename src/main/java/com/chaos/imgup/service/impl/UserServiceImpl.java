package com.chaos.imgup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chaos.imgup.dto.LoginDTO;
import com.chaos.imgup.dto.RegisterDTO;
import com.chaos.imgup.dto.UpdatePasswordDTO;
import com.chaos.imgup.dto.UpdateProfileDTO;
import com.chaos.imgup.entity.User;
import com.chaos.imgup.mapper.UserMapper;
import com.chaos.imgup.service.UserService;
import com.chaos.imgup.util.AuthUtil;
import com.chaos.imgup.util.JwtUtil;
import com.chaos.imgup.vo.LoginVO;
import com.chaos.imgup.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    // 存储已失效的刷新令牌，用于登出功能
    private final ConcurrentHashMap<String, Long> invalidatedRefreshTokens = new ConcurrentHashMap<>();

    @Override
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", registerDTO.getUsername())) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查邮箱是否已存在
        if (userMapper.selectOne(new QueryWrapper<User>().eq("email", registerDTO.getEmail())) != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        // 加密密码
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 使用Spring Security的AuthenticationManager进行用户认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(userDetails.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());
        return new LoginVO(accessToken, refreshToken, jwtUtil.getAccessTokenExpiration() / 1000);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 检查刷新令牌是否已失效
        if (invalidatedRefreshTokens.containsKey(refreshToken)) {
            throw new RuntimeException("Refresh token has been invalidated");
        }

        // 验证刷新令牌
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);

        // 生成新的访问令牌和刷新令牌
        String newAccessToken = jwtUtil.generateAccessToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        // 将旧的刷新令牌加入失效列表
        invalidatedRefreshTokens.put(refreshToken, jwtUtil.extractExpiration(refreshToken).getTime());

        return new LoginVO(newAccessToken, newRefreshToken, jwtUtil.getAccessTokenExpiration() / 1000);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && jwtUtil.validateRefreshToken(refreshToken)) {
            invalidatedRefreshTokens.put(refreshToken, jwtUtil.extractExpiration(refreshToken).getTime());
        }
    }

    @Override
    public UserInfoVO getUserByUsername() {
        User user = AuthUtil.getCurrentUser();
        return new UserInfoVO(user.getUsername(), user.getNickname(), user.getAvatar(), user.getEmail(), user.getCreateTime());
    }

    @Override
    @Transactional
    public void updateProfile(UpdateProfileDTO dto) {
        User currentUser = AuthUtil.getCurrentUser();
        User user = userMapper.selectById(currentUser.getId());

        // 检查邮箱是否被修改
        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(user.getEmail())) {
            // 检查新邮箱是否已被占用
            if (userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getEmail())) != null) {
                throw new RuntimeException("该邮箱已被注册");
            }
            user.setEmail(dto.getEmail());
        }

        // 更新非空字段
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }

        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(UpdatePasswordDTO dto) {
        User currentUser = AuthUtil.getCurrentUser();
        User user = userMapper.selectById(currentUser.getId());

        // 1. 验证旧密码是否正确
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }
        // 2. 验证新密码是否为空
        if (!StringUtils.hasText(dto.getNewPassword())) {
            throw new RuntimeException("新密码不能为空");
        }
        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }


    // 定时任务：每天凌晨清理过期的黑名单令牌
    @Scheduled(cron = "0 0 0 * * ?")
    private void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        // 移除所有已过期的令牌
        invalidatedRefreshTokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}