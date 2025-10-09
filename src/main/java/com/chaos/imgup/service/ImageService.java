package com.chaos.imgup.service;

import com.chaos.imgup.vo.UploadResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    UploadResultVO uploadImage(MultipartFile file, Long ossConfigId);
}

