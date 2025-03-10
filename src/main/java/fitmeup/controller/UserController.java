package fitmeup.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fitmeup.dto.UserDTO;
import fitmeup.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
	
	 private final UserService userService; // ✅ UserService 필드 추가 (자동 주입)

    @GetMapping("/login")
    public String loginForm() {
        return "user/login"; // templates/user/login.html
    }
    
    @GetMapping("/roleSelection")
    public String roleSelection() {
        return "user/roleSelection"; // templates/user/roleSelection.html
    }

    @GetMapping("/userJoin")
    public String userJoin(@RequestParam(name="role", required=false, defaultValue="User") String role,
                           Model model) {
        model.addAttribute("role", role);
        return "user/userJoin";
    }
    
    @PostMapping("/joinProc")
    public String joinProcess(@ModelAttribute UserDTO userDTO) {
        boolean success = userService.joinProc(userDTO);
        if (success) {
            return "redirect:/user/login"; // 회원가입 성공 시 로그인 페이지로 이동
        } else {
            return "redirect:/user/join?error"; // 실패 시 다시 회원가입 페이지로
        }
    }
        @GetMapping("/pendingTrainer")
        public String pendingTrainer() {
            return "user/pendingTrainer"; // templates/user/pendingTrainer.html
        }
        
        @GetMapping("/findId")
        public String findIdForm() {
            return "user/findId"; // 아이디 찾기 페이지 이동 (GET 요청)
        }

        
        @PostMapping("/findId")
        public String findId(@RequestParam("userName") String userName,
                             @RequestParam("userContact") String userContact,
                             Model model) {
            String email = userService.findUserEmail(userName, userContact);

            if ("존재하지 않는 회원정보입니다.".equals(email)) {
                model.addAttribute("error", email); // 에러 메시지
            } else {
                model.addAttribute("email", email); // 정상 이메일
            }

            return "user/findId";
        }
        
}
    

