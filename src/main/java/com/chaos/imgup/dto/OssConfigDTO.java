package com.chaos.imgup.dto;

import lombok.Data;
import java.util.Map;

@Data
public class OssConfigDTO {
    private String configName; // 配置备注名, e.g., "我的阿里云主账号"
    private String ossType;    // 图床类型, e.g., "ALIYUN", "TENCENT"
    private Boolean isDefault; // 是否设为默认
    // 使用Map接收不同图床的专属配置项
    // e.g., {"accessKeyId": "xxx", "accessKeySecret": "yyy", "bucketName": "zzz", "endpoint": "..."}
    private Map<String, String> configDetail;
}
