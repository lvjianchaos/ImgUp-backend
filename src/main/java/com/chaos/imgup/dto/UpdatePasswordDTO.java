package com.chaos.imgup.dto;

import lombok.Data;

@Data
public class UpdatePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
