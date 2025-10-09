package com.chaos.imgup.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // 使用Builder模式方便创建对象
public class UploadResultVO {
    private String url;
    private String originalName;
    private Long size;
}