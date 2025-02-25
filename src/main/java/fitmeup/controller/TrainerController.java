package fitmeup.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.service.TrainerApplicationService;
import fitmeup.service.TrainerService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TrainerController { 

    private final TrainerService trainerService;
    private final TrainerApplicationService consultationService;

    @GetMapping({"/", "", "/trainers"})
    public String trainers(Model model) {
        List<TrainerEntity> trainers = trainerService.getAllTrainers();
        model.addAttribute("trainers", trainers);
        return "trainers"; 
    }

    @GetMapping("/trainer/{trainerId}")
    public String trainerDetail(@PathVariable("trainerId") Long trainerId, Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        TrainerEntity trainer = trainerService.getTrainerById(trainerId);
        List<TrainerPhotoEntity> photos = trainerService.getTrainerPhotos(trainerId);

        // ✅ 로그인 여부 확인
        boolean loggedIn = (userDetails != null);
        model.addAttribute("loggedIn", loggedIn);

        // ✅ 상담 신청 여부 확인 (로그인한 사용자만)
        boolean applied = false;
        if (loggedIn) {
            String userEmail = userDetails.getUsername(); // 현재 로그인한 사용자의 이메일 가져오기
            applied = consultationService.isAlreadyApplied(userEmail, trainerId);
        }
        model.addAttribute("applied", applied);

        model.addAttribute("trainer", trainer);
        model.addAttribute("photos", photos);
        return "trainer-detail";
    }
}
