package fitmeup.controller;

import java.util.List;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fitmeup.dto.TrainerDTO;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.service.TrainerApplicationService;
import fitmeup.service.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j

public class TrainerController { 

    private final TrainerService trainerService;
    private final TrainerApplicationService consultationService;

    @GetMapping({"/", "", "/trainers"})
    public String trainers(Model model) {
        List<TrainerEntity> trainers = trainerService.getAllTrainers();
        model.addAttribute("trainers", trainers);
        return "trainers"; 
    }

 // URL 변경: /trainer/detail/{trainerId} 로 수정
    @GetMapping("/detail/{trainerId}")
    public String trainerDetail(@PathVariable("trainerId") Long trainerId, Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        TrainerEntity trainer = trainerService.getTrainerById(trainerId);
        List<TrainerPhotoEntity> photos = trainerService.getTrainerPhotos(trainerId);

        boolean loggedIn = (userDetails != null);
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
            String userEmail = userDetails.getUsername();
            model.addAttribute("applied", consultationService.isAlreadyApplied(userEmail, trainerId));
        }
        model.addAttribute("trainer", trainer);
        model.addAttribute("photos", photos);
        return "trainer-detail";
    }
    
    @GetMapping("/trainerJoin")
    public String trainerJoin(Model model) {
        // 필요시 추가 모델 속성 설정
        return "user/trainerJoin";  // templates/user/trainerJoin.html 파일을 렌더링
    }

    @PostMapping("/trainer/joinProc")
    public String joinProcess(@ModelAttribute TrainerDTO trainerDTO, Model model) {
        boolean success = trainerService.joinProc(trainerDTO);
        if (success) {
            // 가입 신청 성공 시 "가입 신청 완료" 페이지로 이동
            return "redirect:/trainer/joinPending";
        } else {
            // 실패 시 다시 회원가입 페이지로 (에러 메시지 추가 가능)
            return "redirect:/trainer/join?error";
        }
    }

    @GetMapping("/trainer/joinPending")
    public String joinPending(Model model) {
        model.addAttribute("message", "가입 신청이 완료되었습니다. 관리자의 승인을 기다려주세요.");
        return "user/pendingTrainer"; // 이 파일을 생성하여 안내 메시지를 표시
    }


@GetMapping("/trainerschedule")
public String trainerSchedule(Model model	) {
    // 필요한 모델 속성이 있다면 여기서 추가 (예: 일정 정보 등)
    return "schedule/trainerschedule"; // templates 폴더 안에 trainerschedule.html 파일이 있어야 합니다.
}
}
