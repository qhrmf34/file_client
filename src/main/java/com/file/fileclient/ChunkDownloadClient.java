package com.file.fileclient;

import com.file.fileclient.service.ChecksumService;
import com.file.fileclient.service.ChunkDownloadService;
import com.file.fileclient.service.FileService;

import java.nio.file.Files;

public class ChunkDownloadClient
{

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("사용법: java ChunkDownloadClient <filename>");
            System.out.println("예:    java ChunkDownloadClient test_50MB.bin");
            return;
        }
        String filename = args[0];
        printBanner(filename);
        FileService fileService = null;
        try
        {
            fileService          = new FileService(filename);
            ChunkDownloadService chunkDownloadService = new ChunkDownloadService(filename, fileService);
            ChecksumService      checksumService      = new ChecksumService(fileService.getOutputPath());

            long elapsedMs = chunkDownloadService.executeDownloadLoop();
            long fileSize = fileService.getFileSize();
            System.out.printf("%n[Client] 다운로드 완료 | 파일크기=%,d bytes | 소요시간=%,d ms%n",
                    fileSize, elapsedMs);

            boolean verified = checksumService.verify(chunkDownloadService.getServerChecksum());
            if (!verified)
            {
                Files.deleteIfExists(fileService.getOutputPath());
                throw new RuntimeException("무결성 검증 실패 - 파일이 삭제되었습니다.");
            }

        }
        catch (Exception e)
        {
            if (fileService != null)
            {
                try
                {
                    Files.deleteIfExists(fileService.getOutputPath());
                    System.err.println("\n[Client] 오류 발생으로 다운로드 파일이 삭제되었습니다.");
                }
                catch (Exception deleteException)
                {
                    System.err.println("\n[Client] 파일 삭제 중 오류: " + deleteException.getMessage());
                }
            }
            System.err.println("\n[Client] 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void printBanner(String filename)
    {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║       Chunked File Download Client               ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf( "║  파일명  : %-38s ║%n", filename);
        System.out.printf( "║  저장경로: %-38s ║%n", "./downloads/" + filename);
        System.out.printf( "║  서버URL : %-38s ║%n", "http://localhost:8080");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
    }
}