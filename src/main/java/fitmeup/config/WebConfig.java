package fitmeup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Value("${upload.meal.path}")
    private String mealUploadDir;
    
    @Value("${upload.video.path}")
    private String videoUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/meal/**")
                .addResourceLocations("file:///" + mealUploadDir);
        
        registry.addResourceHandler("/uploads/video/**")
                .addResourceLocations("file:///" + videoUploadDir);

        registry.addResourceHandler("/uploads/chat/**")
            .addResourceLocations("file:///" + videoUploadDir);
    }
}

/*
 업로드된 이미지 정적 리소스로 제공
Spring Boot는 기본적으로 src/main/resources/static/ 폴더 안에 있는 파일만 정적 리소스로 제공해.
하지만 우리는 업로드된 이미지를 c:/uploadPath/에 저장하고 있으니까,
Spring Boot에게 "이 경로도 정적 파일로 제공해 줘!" 라고 알려줘야 해.s
 */
 
/*
 addResourceHandler("/uploads/meal/**")
→ 브라우저에서 /uploads/meal/파일명으로 접근 가능하도록 설정
 addResourceLocations("file:///c:/uploadPath/")
→ 실제 파일이 저장되는 로컬 디렉터리와 연결

*/
