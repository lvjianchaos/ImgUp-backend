package com.chaos.imgup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chaos.imgup.dto.LoginDTO;
import com.chaos.imgup.dto.RegisterDTO;
import com.chaos.imgup.entity.User;
import com.chaos.imgup.mapper.UserMapper;
import com.chaos.imgup.service.UserService;
import com.chaos.imgup.util.JwtUtil;
import com.chaos.imgup.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        final String token = jwtUtil.generateToken(userDetails);

        return new LoginVO(token);
    }
}