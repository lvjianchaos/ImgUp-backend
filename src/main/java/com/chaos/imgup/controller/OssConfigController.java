package com.chaos.imgup.controller;

import com.chaos.imgup.common.Result;
import com.chaos.imgup.dto.OssConfigDTO;
import com.chaos.imgup.service.OssConfigService;
import com.chaos.imgup.vo.OssConfigVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/oss-config")
public class OssConfigController {

    @Autowired
    private OssConfigService ossConfigService;

    @PostMapping
    public Result<?> addConfig(@RequestBody OssConfigDTO ossConfigDTO) {
        ossConfigService.addConfig(ossConfigDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<OssConfigVO>> listConfigs() {
        return Result.success(ossConfigService.listConfigs());
    }

    @GetMapping("/list/{type}")
    public Result<List<OssConfigVO>> listConfigsByType(@PathVariable String type) {
        return Result.success(ossConfigService.listConfigsByType(type));
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, String>> getConfigDetail(@PathVariable Long id) {
        return Result.success(ossConfigService.getConfigDetail(id));
    }

    @PutMapping("/{id}")
    public Result<?> updateConfig(@PathVariable Long id, @RequestBody OssConfigDTO ossConfigDTO) {
        ossConfigService.updateConfig(id, ossConfigDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteConfig(@PathVariable Long id) {
        if(ossConfigService.deleteConfig(id)) {
            return Result.success();
        }
        return Result.error(500,"This Config has undeleted images, Please delete them first!");
    }

    @PutMapping("/default/{id}")
    public Result<?> setDefaultConfig(@PathVariable Long id) {
        ossConfigService.setDefaultConfig(id);
        return Result.success();
    }
}
