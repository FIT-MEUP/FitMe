package fitmeup.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FileUploadConfig {
	
	@Value("${upload.meal.path}")
	private String mealUploadDir;

	@Value("${upload.video.path}")
	private String videoUploadDir;


	@PostConstruct
	public void createUploadDirectory() {
	    // 음식 게시판 업로드 폴더 생성
	    Path mealPath = Paths.get(mealUploadDir);
	    // 운동 게시판 업로드 폴더 생성
	    Path videoPath = Paths.get(videoUploadDir);
	    try {
	        if (!Files.exists(mealPath)) {
	            Files.createDirectories(mealPath);
	            System.out.println("Meal upload directory created at: " + mealUploadDir);
	        }
	        if (!Files.exists(videoPath)) {
	            Files.createDirectories(videoPath);
	            System.out.println("Video upload directory created at: " + videoUploadDir);
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("Could not create upload folders!", e);
	    }
	}
}

/*
 서버 실행 시 c:/uploadPath/ 폴더가 없으면 자동 생성
파일 업로드 기능과 관련됨 (파일이 저장될 폴더를 보장하는 역할)
이걸 삭제하면 폴더가 없어서 업로드 오류 발생 가능
 */
