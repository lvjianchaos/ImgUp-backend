package com.chaos.imgup.controller;

import com.chaos.imgup.common.Result;
import com.chaos.imgup.dto.*;
import com.chaos.imgup.service.UserService;
import com.chaos.imgup.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    // 刷新令牌接口
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestBody RefreshDTO refreshDTO) {
        LoginVO refreshVO = userService.refreshToken(refreshDTO.getRefreshToken());
        return Result.success(refreshVO);
    }

    // 登出接口
    @PostMapping("logout")
    public Result<?> logout(@RequestBody RefreshDTO refreshDTO) {
        userService.logout(refreshDTO.getRefreshToken());
        return Result.success("登出成功");
    }

    // 获取用户信息
    @GetMapping("info")
    public Result<?> userInfo() {
        return Result.success(userService.getUserByUsername());
    }

    @PutMapping("password")
    public Result<?> updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
        userService.updatePassword(updatePasswordDTO);
        return Result.success("修改密码成功");
    }

    @PutMapping("profile")
    public Result<?> updateProfile(@RequestBody UpdateProfileDTO updateProfileDTO) {
        userService.updateProfile(updateProfileDTO);
        return Result.success("修改基本信息成功");
    }
}