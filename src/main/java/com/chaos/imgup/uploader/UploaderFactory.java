package com.chaos.imgup.uploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UploaderFactory {

    private final Map<String, Uploader> uploaderMap = new HashMap<>();

    @Autowired
    public UploaderFactory(List<Uploader> uploaders) {
        for (Uploader uploader : uploaders) {
            uploaderMap.put(uploader.getOssType(), uploader);
        }
    }

    public Uploader getUploader(String ossType) {
        Uploader uploader = uploaderMap.get(ossType);
        if (uploader == null) {
            throw new IllegalArgumentException("不支持的图床类型: " + ossType);
        }
        return uploader;
    }
}