package com.chaos.imgup.uploader.impl;

import com.chaos.imgup.uploader.UploadResponse;
import com.chaos.imgup.uploader.Uploader;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Component
public class QiniuUploader implements Uploader {

    @Override
    public UploadResponse upload(MultipartFile file, Map<String, String> config) throws Exception {
        String accessKey = config.get("accessKey");
        String secretKey = config.get("secretKey");
        String bucketName = config.get("bucketName");
        String domain = config.get("domain"); // 七牛云需要配置访问域名

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucketName);

        // 自动识别区域
        Configuration cfg = new Configuration(Region.autoRegion());
        UploadManager uploadManager = new UploadManager(cfg);

        try (InputStream inputStream = file.getInputStream()) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storageName = UUID.randomUUID().toString() + fileExtension;

            uploadManager.put(inputStream, storageName, upToken, null, null);

            // 拼接URL
            String url = domain + "/" + storageName;

            return new UploadResponse(url, storageName);
        }
    }

    @Override
    public void delete(String storageName, Map<String, String> config) throws Exception {
        String accessKey = config.get("accessKey");
        String secretKey = config.get("secretKey");
        String bucketName = config.get("bucketName");

        Auth auth = Auth.create(accessKey, secretKey);
        Configuration cfg = new Configuration(Region.autoRegion());
        BucketManager bucketManager = new BucketManager(auth, cfg);

        bucketManager.delete(bucketName, storageName);
    }

    @Override
    public String getOssType() {
        return "QINIU";
    }
}
