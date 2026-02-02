package com.file.fileclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DownloadResponse
{
    @JsonProperty("type")
    private String type;
    @JsonProperty("filename")
    private String filename;
    @JsonProperty("seq")
    private int seq;
    @JsonProperty("data") // Base64 문자열
    private String data;
    @JsonProperty("sentBytes")
    private long sentBytes;

    // ─── 상태 판정 ────────────────────────────────────────────
    public boolean isEnd()
    {
        return seq == -1 && "response".equals(type); // seq가 -1이면 종료
    }
    public boolean isError()
    {
        return "error".equals(type);     // type이 error면 에러
    }
}
