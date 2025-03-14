package fitmeup.controller;

import fitmeup.dto.ChatMessage;
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
  public ResponseEntity<Resource> downloadFile(@PathVariable String savedFileName) throws IOException {
    // 파일 다운로드를 위해 savedFileName에 해당하는 메시지를 조회
    Optional<ChatMessage> optional = chatService.findBySavedFileName(savedFileName);
    if (optional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    ChatMessage chatMessage = optional.get();
    // ChatMessage에는 원본 파일 이름 정보가 없으므로, 저장된 파일명(savedFileName)을 기본값으로 사용
    String originalName = savedFileName;

    // 실제 파일 경로 설정
    Path filePath = Paths.get(uploadDir, savedFileName);
    if (!Files.exists(filePath)) {
      return ResponseEntity.notFound().build();
    }

    // MIME 타입 추론, 기본값은 application/octet-stream
    String contentType = Files.probeContentType(filePath);
    if (contentType == null) {
      contentType = "application/octet-stream";
    }

    // Resource 객체 생성
    Resource resource = new InputStreamResource(Files.newInputStream(filePath));

    // Content-Disposition 헤더 설정
    String encodedFilename = URLEncoder.encode(originalName, StandardCharsets.UTF_8)
        .replace("+", "%20");

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + encodedFilename + "\"")
        .body(resource);
  }
}
