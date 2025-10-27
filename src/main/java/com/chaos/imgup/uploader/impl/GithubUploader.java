package com.chaos.imgup.uploader.impl;

import com.chaos.imgup.uploader.UploadResponse;
import com.chaos.imgup.uploader.Uploader;
import org.kohsuke.github.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.UUID;

@Component
public class GithubUploader implements Uploader {

    // 重试次数：遇到409冲突时，最多重试2次（可根据需求调整）
    private static final int RETRY_TIMES = 2;
    // 重试间隔：每次重试前等待500ms，避免高频请求（单位：毫秒）
    private static final long RETRY_INTERVAL = 500;

    @Override
    public UploadResponse upload(MultipartFile file, Map<String, String> config) throws Exception {
        String token = config.get("token");
        String repoName = config.get("repoName"); // 格式: "用户名/仓库名"
        String branch = config.getOrDefault("branch", "main");
        String path = config.getOrDefault("path", ""); // 可选的子目录

        // 1. 初始化GitHub客户端（建议复用，此处为保持原有逻辑暂不修改）
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository repo = github.getRepository(repoName);

        // 2. 生成文件名称和完整路径（保持原有逻辑）
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storageName = UUID.randomUUID().toString() + fileExtension;
        String fullPath = path.isEmpty() ? storageName : (path + "/" + storageName);

        // 3. 带重试逻辑的文件上传（核心修改部分）
        int retryCount = 0;
        while (retryCount <= RETRY_TIMES) {
            try {
                // 3.1 获取远程文件最新SHA：不存在则为null（首次上传）
                String remoteFileSha = getRemoteFileSha(repo, fullPath, branch);

                // 3.2 上传文件：传入最新SHA，解决版本冲突
                repo.createContent()
                        .path(fullPath)
                        .content(file.getBytes())
                        .message("Upload image: " + storageName)
                        .branch(branch)
                        .sha(remoteFileSha) // 关键：传入远程最新SHA
                        .commit();

                // 上传成功，生成CDN链接并返回
                String url = "https://cdn.jsdelivr.net/gh/" + repoName + "@" + branch + "/" + fullPath;
                return new UploadResponse(url, fullPath);

            } catch (HttpException e) {
                // 3.3 只处理409冲突，其他错误直接抛出
                if (e.getResponseCode() == 409 && retryCount < RETRY_TIMES) {
                    retryCount++;
                    Thread.sleep(RETRY_INTERVAL); // 等待后重试
                } else {
                    throw e; // 重试次数用尽或非409错误，抛出原异常
                }
            }
        }

        // 理论上不会走到这里（重试次数内会成功或抛出异常）
        throw new Exception("Upload failed after " + RETRY_TIMES + " retries");
    }

    /**
     * 核心辅助方法：获取远程指定路径文件的最新SHA
     * @param repo 仓库对象
     * @param fullPath 文件完整路径（如 "images/xxx.png"）
     * @param branch 分支名
     * @return 存在则返回SHA字符串，不存在则返回null
     */
    private String getRemoteFileSha(GHRepository repo, String fullPath, String branch) {
        try {
            // 尝试获取远程文件：若存在，返回其SHA；若不存在，会抛出FileNotFoundException
            GHContent remoteFile = repo.getFileContent(fullPath, branch);
            return remoteFile.getSha();
        } catch (FileNotFoundException e) {
            // 文件不存在（首次上传场景），返回null（GitHub API允许null表示新建）
            return null;
        } catch (Exception e) {
            // 其他异常（如网络问题），直接抛出
            throw new RuntimeException("Failed to get remote file SHA", e);
        }
    }

    // 删除方法无需修改（原有逻辑已通过getFileContent获取最新SHA）
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