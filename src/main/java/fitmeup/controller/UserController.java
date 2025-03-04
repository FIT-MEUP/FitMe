package fitmeup.controller;

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

    @GetMapping("/join")
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


}

