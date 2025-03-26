package fitmeup.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import fitmeup.dto.ChatDTO;
import fitmeup.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DownloadController {

  @Value("${upload.chat.path}")
  private String uploadDir;

  private final ChatService chatService;

  @GetMapping("/download/{savedFileName}")
  public ResponseEntity<Resource> downloadFile(@PathVariable(name = "savedFileName")String savedFileName) throws IOException {
    // 1) DB 조회
    Optional<ChatDTO> optional = chatService.findBySavedFileName(savedFileName);
    if (optional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    ChatDTO chatDTO = optional.get();

    // 2) originalFileName 존재 여부
    String originalName = (chatDTO.getOriginalFileName() != null && !chatDTO.getOriginalFileName().isEmpty())
        ? chatDTO.getOriginalFileName()
        : savedFileName;

    // 3) 파일 경로
    Path filePath = Paths.get(uploadDir, savedFileName);
    if (!Files.exists(filePath)) {
      return ResponseEntity.notFound().build();
    }

    // 4) MIME 타입
    String contentType = Files.probeContentType(filePath);
    if (contentType == null) {
      contentType = "application/octet-stream";
    }

    // 5) Resource 생성
    Resource resource = new InputStreamResource(Files.newInputStream(filePath));

    // 6) 파일명 인코딩 (공백이 '+'로 치환되므로 replace 처리)
    String encodedFilename = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");

    // 7) Content-Disposition 헤더 설정
    //    filename=... 과 filename*=... 을 함께 쓰면,
    //    최신 브라우저에서 UTF-8 파일명을 잘 처리하고, 구형 브라우저는 filename= 를 사용합니다.
    String contentDispositionValue =
        "attachment; " +
            "filename=\"" + encodedFilename + "\"; " +
            "filename*=UTF-8''" + encodedFilename;

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionValue)
        .body(resource);
  }
}
