package com.chaos.imgup.service;

import com.chaos.imgup.dto.LoginDTO;
import com.chaos.imgup.dto.RegisterDTO;
import com.chaos.imgup.vo.LoginVO;

public interface UserService {
    void register(RegisterDTO registerDTO);
    LoginVO login(LoginDTO loginDTO);
}
