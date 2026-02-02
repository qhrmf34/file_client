package com.file.fileclient.service;

import java.io.*;
import java.nio.file.*;
/*
FileService — 파일 I/O 담당. ./downloads/ 폴더에 파일을 생성하고, 각 청크를 append 모드
 */
public class FileService
{
    private static final String DOWNLOAD_DIR = "./downloads/";
    private final Path outputPath; // 저장할 파일 경로
    public FileService(String filename) {
        this.outputPath = Paths.get(DOWNLOAD_DIR, filename);
    }

    // ─── 다운로드 시작 전 준비 ─────────────────────────────────
    public void prepare() throws IOException
    {
        Files.createDirectories(outputPath.getParent());
        if (Files.exists(outputPath))
        {
            Files.delete(outputPath);
            System.out.println("[FileService] 기존 파일 삭제: " + outputPath);
        }
    }

    // ─── chunk를 파일에 append ─────────────────────────────────
    public void appendChunk(byte[] chunkBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile(), true)) {
            fos.write(chunkBytes);
            fos.flush();
        }
    }

    // ─── 파일 정보 조회 ───────────────────────────────────────
    public long getFileSize() throws IOException
    {
        return Files.size(outputPath);
    }
    public Path getOutputPath() {
        return outputPath;
    }
}
