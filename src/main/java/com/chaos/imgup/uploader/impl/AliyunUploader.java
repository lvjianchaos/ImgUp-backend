package com.chaos.imgup.uploader.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.chaos.imgup.uploader.UploadResponse;
import com.chaos.imgup.uploader.Uploader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Component
public class AliyunUploader implements Uploader {

    @Override
    public UploadResponse upload(MultipartFile file, Map<String, String> config) throws Exception {
        String endpoint = config.get("endpoint");
        String accessKeyId = config.get("accessKeyId");
        String accessKeySecret = config.get("accessKeySecret");
        String bucketName = config.get("bucketName");

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try (InputStream inputStream = file.getInputStream()) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 即 storageName
            String objectName = UUID.randomUUID().toString() + fileExtension;

            ossClient.putObject(bucketName, objectName, inputStream);

            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;

            return new UploadResponse(url, objectName);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void delete(String storageName, Map<String, String> config) throws Exception {
        String endpoint = config.get("endpoint");
        String accessKeyId = config.get("accessKeyId");
        String accessKeySecret = config.get("accessKeySecret");
        String bucketName = config.get("bucketName");

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.deleteObject(bucketName, storageName);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public String getOssType() {
        return "ALIYUN";
    }
}
