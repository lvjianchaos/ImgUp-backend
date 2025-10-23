package com.chaos.imgup.uploader.impl;

import com.chaos.imgup.uploader.UploadResponse;
import com.chaos.imgup.uploader.Uploader;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Component
public class TencentUploader implements Uploader {

    @Override
    public UploadResponse upload(MultipartFile file, Map<String, String> config) throws Exception {
        String secretId = config.get("secretId");
        String secretKey = config.get("secretKey");
        String regionName = config.get("region");
        String bucketName = config.get("bucketName");

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try (InputStream inputStream = file.getInputStream()) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storageName = UUID.randomUUID().toString() + fileExtension;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());

            cosClient.putObject(bucketName, storageName, inputStream, objectMetadata);

            String url = "https://" + bucketName + ".cos." + regionName + ".myqcloud.com/" + storageName;

            return new UploadResponse(url, storageName);
        } finally {
            cosClient.shutdown();
        }
    }

    @Override
    public void delete(String storageName, Map<String, String> config) throws Exception {
        String secretId = config.get("secretId");
        String secretKey = config.get("secretKey");
        String regionName = config.get("region");
        String bucketName = config.get("bucketName");

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            cosClient.deleteObject(bucketName, storageName);
        } finally {
            cosClient.shutdown();
        }
    }

    @Override
    public String getOssType() {
        return "TENCENT";
    }
}
