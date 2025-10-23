package com.chaos.imgup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private LocalDateTime createTime;
}
