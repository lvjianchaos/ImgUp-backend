package com.chaos.imgup.controller;

import com.chaos.imgup.common.Result;
import com.chaos.imgup.service.ImageService;
import com.chaos.imgup.vo.UploadResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private ImageService imageService;

    @PostMapping
    public Result<List<UploadResultVO>> upload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "configId", required = false) Long configId
    ) {
        if (files == null || files.length == 0) {
            return Result.error(400, "上传文件不能为空");
        }
        List<UploadResultVO> resultVOs = imageService.uploadImages(files, configId);
        return Result.success(resultVOs);
    }
}