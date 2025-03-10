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
import fitmeup.entity.UserEntity;
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

        boolean loggedIn = (userDetails != null);
        model.addAttribute("loggedIn", loggedIn);

        boolean applied = false;
        boolean isTrainer = false; // ✅ 트레이너 본인 여부

        if (loggedIn) {
            String userEmail = userDetails.getUsername();
            applied = consultationService.isAlreadyApplied(userEmail, trainerId);

            // ✅ 트레이너 본인인지 확인
            UserEntity loggedInUser = trainerService.getUserByEmail(userEmail);
            isTrainer = (loggedInUser.equals(trainer.getUser())); 
        }

        model.addAttribute("applied", applied);
        model.addAttribute("isTrainer", isTrainer);
        model.addAttribute("trainer", trainer);
        model.addAttribute("photos", photos);

        return "trainer-detail";
    }
}
