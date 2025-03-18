package fitmeup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VideoController {

    @GetMapping("/videos")
    public String workoutVideos() {
        return "videos"; // videos.html 페이지로 이동
    }
}