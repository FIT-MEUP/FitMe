package fitmeup.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import fitmeup.service.AdminService;
import fitmeup.dto.UserDTO;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/adminpage")
    public String adminpage(Model model) {
        model.addAttribute("trainerList", adminService.getTrainers());
        model.addAttribute("pendingTrainerList", adminService.getPendingTrainers());
        model.addAttribute("userList", adminService.getUsers());
        return "admin/adminpage";
    }

    @DeleteMapping("/deleteTrainer/{id}")
    @ResponseBody
    public String deleteTrainer(@PathVariable Long id) {
        adminService.deleteTrainer(id);
        return "{\"message\": \"트레이너 삭제 완료\"}";
    }

    @PutMapping("/approveTrainer/{id}")
    @ResponseBody
    public String approveTrainer(@PathVariable Long id) {
        adminService.approveTrainer(id);
        return "{\"message\": \"트레이너 승인 완료\"}";
    }

    @DeleteMapping("/rejectTrainer/{id}")
    @ResponseBody
    public String rejectTrainer(@PathVariable Long id) {
        adminService.rejectTrainer(id);
        return "{\"message\": \"트레이너 승인 거절 완료\"}";
    }

    @DeleteMapping("/deleteUser/{id}")
    @ResponseBody
    public String deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return "{\"message\": \"회원 삭제 완료\"}";
    }

    @PostMapping("/updateNotice")
    @ResponseBody
    public String updateNotice(@RequestBody String notice) {
        adminService.updateNotice(notice);
        return "{\"message\": \"공지사항 업데이트 완료\"}";
    }
}
