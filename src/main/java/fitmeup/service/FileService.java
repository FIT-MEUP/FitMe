package fitmeup.service;

import fitmeup.dto.UploadResultDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileService {

  // application.properties 또는 yml에 설정된 파일 업로드 경로
  @Value("${upload.chat.path}")
  private String uploadPath;

  /**
   * 파일을 지정된 업로드 경로에 저장하고, 업로드 결과 DTO를 생성합니다.
   *
   * @param file 클라이언트에서 전송한 MultipartFile
   * @return UploadResultDTO 업로드 결과 정보(원본 파일명, 저장 파일명, 파일 타입, 파일 URL)
   * @throws IOException 파일 저장 중 발생할 수 있는 예외
   */
  public UploadResultDTO saveToLocal(MultipartFile file) throws IOException {
    String originalName = file.getOriginalFilename();
    String contentType = file.getContentType();

    // 파일 확장자 추출
    String extension = extractExtension(originalName);
    // UUID를 사용해 유니크한 저장 파일명 생성
    String savedFileName = UUID.randomUUID().toString() + "." + extension;

    // MIME 타입에 따라 파일 타입 분류
    String fileType = determineFileType(contentType);

    // 업로드 파일을 저장할 최종 디렉토리 생성
    Path targetDir = Paths.get(uploadPath);
    if (!Files.exists(targetDir)) {
      Files.createDirectories(targetDir);
    }
    Path targetPath = targetDir.resolve(savedFileName);

    // 파일을 최종 디렉토리로 저장
    file.transferTo(targetPath.toFile());

    // 다운로드는 별도의 DownloadController를 통해 처리하므로,
    // 파일 URL은 "/download/{저장된파일명}" 형태로 설정
    String fileUrl = "/download/" + savedFileName;

    return UploadResultDTO.builder()
        .originalFileName(originalName)
        .savedFileName(savedFileName)
        .fileType(fileType)
        .fileUrl(fileUrl)
        .build();
  }

  /**
   * 파일명에서 확장자를 추출합니다.
   * 예) "example.png" -> "png"
   *
   * @param fileName 원본 파일명
   * @return 소문자 형태의 확장자 문자열
   */
  private String extractExtension(String fileName) {
    if (fileName == null) return "";
    int dotPos = fileName.lastIndexOf(".");
    if (dotPos == -1) {
      return "";
    }
    return fileName.substring(dotPos + 1).toLowerCase();
  }

  /**
   * MIME 타입을 기반으로 파일의 대략적인 타입을 분류합니다.
   * image/*, video/*, audio/*이면 각각 해당 타입을 반환하고, 그 외에는 document로 분류합니다.
   *
   * @param contentType 파일의 MIME 타입
   * @return "image", "video", "audio", "document" 중 하나
   */
  private String determineFileType(String contentType) {
    if (contentType == null) {
      return "document";
    }
    contentType = contentType.toLowerCase();
    if (contentType.startsWith("image/")) {
      return "image";
    } else if (contentType.startsWith("video/")) {
      return "video";
    } else if (contentType.startsWith("audio/")) {
      return "audio";
    }
    return "document";
  }
}
