package com.chaos.imgup.uploader;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface Uploader {
    /**
     * 上传文件
     * @param file 待上传的文件
     * @param config 解密后的配置详情Map
     * @return 包含URL和StorageName的响应对象
     * @throws Exception 上传过程中发生的异常
     */
    UploadResponse upload(MultipartFile file, Map<String, String> config) throws Exception;

    /**
     * 获取当前策略对应的OSS类型
     * @return "ALIYUN", "TENCENT", etc.
     */
    String getOssType();
}