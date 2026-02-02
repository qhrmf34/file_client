package com.file.fileclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadRequest
{
    @JsonProperty("type")
    private final String type; //항상 request
    @JsonProperty("filename")
    private final String filename;
    @JsonProperty("seq")
    private final int seq;
    @JsonProperty("data")
    private final String data; //항상 null

    public DownloadRequest(String filename, int seq)
    {
        this.type     = "request";
        this.filename = filename;
        this.seq      = seq;
        this.data     = null;   //다운로드에선 미사용
    }
}
