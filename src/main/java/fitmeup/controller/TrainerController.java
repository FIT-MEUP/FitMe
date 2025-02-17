package fitmeup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import fitmeup.service.TrainerService;
import fitmeup.entity.TrainerEntity;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @GetMapping("/trainers")
    public String trainers(Model model) {
        List<TrainerEntity> trainers = trainerService.getAllTrainers();
        model.addAttribute("trainers", trainers);
        return "trainers"; // templates/trainers.html을 렌더링
    }
}
