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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fitmeup.dto.TrainerDTO;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.entity.UserEntity;
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
    
    @GetMapping("/trainerJoin")
    public String trainerJoin(Model model) {
        // 필요시 추가 모델 속성 설정
        return "user/trainerJoin";  // templates/user/trainerJoin.html 파일을 렌더링
    }

    @PostMapping("/trainer/joinProc")
    public String joinProcess(@ModelAttribute TrainerDTO trainerDTO, RedirectAttributes redirectAttributes) {
        try {
            boolean success = trainerService.joinProc(trainerDTO);
            if (success) {
                return "redirect:/trainer/joinPending"; // 가입 성공 시 이동
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 이메일 또는 전화번호입니다!");
                return "redirect:/trainerJoin"; // 가입 실패 시 이동
            }
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다.");
            return "redirect:/trainerJoin"; 
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
