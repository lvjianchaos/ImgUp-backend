package com.chaos.imgup.dto;

import lombok.Data;

@Data
public class PageDTO {
    // 当前页码
    private long current = 1;
    // 每页数量
    private long size = 10;
}

