package fitmeup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResultDTO {
  private String originalFileName;  // 업로드된 파일의 원본 이름
  private String savedFileName;     // 서버에 저장된 파일 이름 (중복 방지를 위해 UUID 등 사용)
  private String fileType;          // "image", "video", "audio", "document" 등
  private String fileUrl;           // 해당 파일의 접근 URL (다운로드 또는 미리보기용)
}
