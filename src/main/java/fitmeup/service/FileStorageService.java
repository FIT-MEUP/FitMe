package fitmeup.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    // 실제 파일 저장 경로 (운영 환경에 맞게 수정)
    private final Path fileStorageLocation;

    public FileStorageService() {
        // "C:/uploadPath/" 경로를 사용
        this.fileStorageLocation = Paths.get("C:/uploadPath/").toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    // 파일 저장: 파일을 저장하고 클라이언트 접근 URL 반환
    public String saveFile(MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                originalFileName = "default.jpg";
            }
            // 고유한 파일 이름 생성 (System.nanoTime()을 사용)
            String fileName = System.nanoTime() + "_" + originalFileName;
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    // 파일 삭제: 클라이언트 URL에서 실제 파일 경로를 계산하여 삭제
    public void deleteFile(String fileUrl) {
        try {
            // fileUrl 예: "/uploads/1742265989245_filename.jpg"
            String fileName = fileUrl.replace("/uploads/", "").trim();
            Path filePath = fileStorageLocation.resolve(fileName);
            System.out.println("삭제 시도 파일 경로: " + filePath.toString());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("파일 삭제 성공: " + filePath.toString());
            } else {
                System.out.println("파일을 찾을 수 없음: " + filePath.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }
}
