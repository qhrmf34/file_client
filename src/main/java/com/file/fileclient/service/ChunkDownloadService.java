package com.file.fileclient.service;

import com.file.fileclient.dto.DownloadRequest;
import com.file.fileclient.dto.DownloadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.net.*;
import java.util.Base64;
/*
while(true) 루프로 seq를 0부터 계속 올려가며 요청을 보내고, 받은 Base64 데이터를 바이트로 디코딩하여 파일에 붙입니다. 서버가 종료 신호를 보내면 루프를 빠져나옵니다.
 */
@Getter
@RequiredArgsConstructor
public class ChunkDownloadService
{
    private static final String       DOWNLOAD_URL = "http://localhost:8080/api/download";
    private static final ObjectMapper MAPPER   = new ObjectMapper();
    private final String      filename;
    private final FileService fileService;
    private String serverChecksum;   // ← 종료 시 체크섬 저장
    // ─── 다운로드 루프 실행 ────────────────────────────────────
    public long executeDownloadLoop() throws Exception
    {
        fileService.prepare();
        long startTime = System.currentTimeMillis();
        int  seq       = 0;

        while (true)
        {
            DownloadRequest  request  = new DownloadRequest(filename, seq);
            DownloadResponse response = sendRequest(request);
            if (response.isError())
            {
                throw new RuntimeException("서버 에러 응답: " + response.getData());
            }
            if (response.isEnd())
            {
                serverChecksum = response.getData();   // ← data에서 체크섬 추출
                System.out.println("\n[ChunkDownloadService] 서버 응답: seq = -1 → 전송 완료 신호 수신");
                break;
            }
            byte[] chunkBytes = Base64.getDecoder().decode(response.getData());
            fileService.appendChunk(chunkBytes);
            System.out.printf("[ChunkDownloadService] seq=%-4d | chunk=%,6d bytes | 누적=%,12d bytes%n",
                    response.getSeq(), chunkBytes.length, response.getSentBytes());
            seq++;
        }
        return System.currentTimeMillis() - startTime;
    }
    // ─── HTTP POST 요청 + 응답 파싱 ────────────────────────────
    private DownloadResponse sendRequest(DownloadRequest request) throws Exception
    {
        String requestJson = MAPPER.writeValueAsString(request);
        HttpURLConnection conn = openPostConnection();
        try (OutputStream os = conn.getOutputStream())
        {
            os.write(requestJson.getBytes("UTF-8"));
        }
        int status = conn.getResponseCode();
        if (status == 404)
        {
            conn.disconnect();
            throw new RuntimeException("파일을 찾을 수 없습니다: " + filename);
        }
        if (status != 200)
        {
            conn.disconnect();
            throw new RuntimeException("서버 응답 오류: HTTP " + status);
        }
        String responseJson;
        try (InputStream is = conn.getInputStream())
        {
            responseJson = new String(is.readAllBytes(), "UTF-8");
        }
        finally
        {
            conn.disconnect();
        }
        return MAPPER.readValue(responseJson, DownloadResponse.class);
    }
    private HttpURLConnection openPostConnection() throws Exception
    {
        HttpURLConnection conn = (HttpURLConnection) new URL(DOWNLOAD_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept",       "application/json");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(30_000);
        return conn;
    }
}