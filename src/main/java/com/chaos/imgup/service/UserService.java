package com.chaos.imgup.service;

import com.chaos.imgup.dto.LoginDTO;
import com.chaos.imgup.dto.RegisterDTO;
import com.chaos.imgup.dto.UpdatePasswordDTO;
import com.chaos.imgup.dto.UpdateProfileDTO;
import com.chaos.imgup.vo.LoginVO;
import com.chaos.imgup.vo.UserInfoVO;

public interface UserService {
    void register(RegisterDTO registerDTO);
    LoginVO login(LoginDTO loginDTO);
    LoginVO refreshToken(String refreshToken);
    void logout(String refreshToken);
    UserInfoVO getUserByUsername();
    void updateProfile(UpdateProfileDTO dto);
    void updatePassword(UpdatePasswordDTO dto);
}
