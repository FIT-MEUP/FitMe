package fitmeup.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FileUploadConfig {
	
	@Value("${spring.servlet.multipart.location}") // application.properties 설정값 가져오기
    private String uploadDir;

	@PostConstruct
    public void createUploadDirectory() {
        Path path = Paths.get(uploadDir);
        try {
            if (!Files.exists(path)) { // 폴더가 없으면 생성
                Files.createDirectories(path);
                System.out.println("Upload directory created at: " + uploadDir);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload folder!", e);
        }
    }

}

/*
 서버 실행 시 c:/uploadPath/ 폴더가 없으면 자동 생성
파일 업로드 기능과 관련됨 (파일이 저장될 폴더를 보장하는 역할)
이걸 삭제하면 폴더가 없어서 업로드 오류 발생 가능
 */
