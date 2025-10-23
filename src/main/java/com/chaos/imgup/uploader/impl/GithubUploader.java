package com.chaos.imgup.uploader.impl;

import com.chaos.imgup.uploader.UploadResponse;
import com.chaos.imgup.uploader.Uploader;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Component
public class GithubUploader implements Uploader {

    @Override
    public UploadResponse upload(MultipartFile file, Map<String, String> config) throws Exception {
        String token = config.get("token");
        String repoName = config.get("repoName"); // 格式: "用户名/仓库名"
        String branch = config.getOrDefault("branch", "main");
        String path = config.getOrDefault("path", ""); // 可选的子目录

        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository repo = github.getRepository(repoName);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storageName = UUID.randomUUID().toString() + fileExtension;
        String fullPath = path.isEmpty() ? storageName : (path + "/" + storageName);

        repo.createContent()
                .path(fullPath)
                .content(file.getBytes())
                .message("Upload image: " + storageName)
                .branch(branch)
                .commit();

        // 使用 jsDelivr CDN URL
        String url = "https://cdn.jsdelivr.net/gh/" + repoName + "@" + branch + "/" + fullPath;

        return new UploadResponse(url, fullPath); // storageName 现在是包含路径的全路径
    }

    @Override
    public void delete(String storageName, Map<String, String> config) throws Exception {
        String token = config.get("token");
        String repoName = config.get("repoName");
        String branch = config.getOrDefault("branch", "main");

        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository repo = github.getRepository(repoName);

        GHContent content = repo.getFileContent(storageName, branch);
        content.delete("Delete image: " + storageName);
    }

    @Override
    public String getOssType() {
        return "GITHUB";
    }
}