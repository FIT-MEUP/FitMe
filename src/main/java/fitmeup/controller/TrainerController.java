package fitmeup.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.service.TrainerService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TrainerController { 

    private final TrainerService trainerService;

    @GetMapping({"/", "", "/trainers"})
    public String trainers(Model model) {
        List<TrainerEntity> trainers = trainerService.getAllTrainers();
        model.addAttribute("trainers", trainers);
        return "trainers"; 
    }

    @GetMapping("/trainer/{trainerId}")
    public String trainerDetail(@PathVariable("trainerId") Long trainerId, Model model) {
        TrainerEntity trainer = trainerService.getTrainerById(trainerId);
        List<TrainerPhotoEntity> photos = trainerService.getTrainerPhotos(trainerId);
        
        model.addAttribute("trainer", trainer);
        model.addAttribute("photos", photos);
        return "trainer-detail";
    }
}
