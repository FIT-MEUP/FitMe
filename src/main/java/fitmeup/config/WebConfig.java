package fitmeup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) { //Spring MVC에게 "특정 경로의 파일들을 정적 리소스로 제공해!" 라고 설정하는 부분
        registry.addResourceHandler("/uploads/meal/**") // 브라우저에서 /uploads/meal/파일명 경로로 요청이 오면,
        												// Spring Boot가 실제 파일이 있는 위치에서 찾아서 제공
                .addResourceLocations("file:///c:/uploadPath/");
        //실제 파일이 저장된 로컬 경로(c:/uploadPath/) 와 연결
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
