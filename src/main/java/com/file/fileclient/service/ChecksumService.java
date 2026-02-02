package com.file.fileclient.service;

import lombok.RequiredArgsConstructor;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
/*
무결성 검증 담당. 다운로드된 파일의 SHA-256 해시를 직접 계산한 후, 서버가 보낸 해시와 비교합니다. 둘이 다르면 전송 중 뭔가 잘못됐다는 의미입니다.
 */
@RequiredArgsConstructor
public class ChecksumService
{
    private final Path localFilePath;

    public boolean verify(String serverChecksum) throws Exception
    {
        System.out.println("\n[ChecksumService] ─── SHA-256 무결성 검증 시작 ───");
        String localChecksum = computeLocalChecksum();
        System.out.println("[ChecksumService] 서버 원본 SHA-256 : " + serverChecksum);
        System.out.println("[ChecksumService] 로컬 파일 SHA-256 : " + localChecksum);
        boolean passed = serverChecksum.equals(localChecksum);
        if (passed)
        {
            System.out.println("\n  ✓ [SUCCESS] 무결성 검증 통과!");
            System.out.println("  → 파일이 손상 없이 정상적으로 다운로드되었습니다.");
        }
        else
        {
            System.out.println("\n  ✗ [FAILURE] 무결성 검증 실패!");
            System.out.println("  → 전송 중 누락 / 순서 오류 / 중복 / Base64 디코딩 문제 의심.");
        }
        return passed;
    }
    // ─── 로컬 파일 SHA-256 계산 ────────────────────────────────
    private String computeLocalChecksum() throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(localFilePath))
        {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1)
            {
                md.update(buffer, 0, len);
            }
        }
        return HexFormat.of().formatHex(md.digest());
    }
}