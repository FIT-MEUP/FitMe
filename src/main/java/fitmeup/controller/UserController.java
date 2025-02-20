package fitmeup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	// 로그인 페이지 GET 요청
	@GetMapping("/login")
	public String loginForm() {
		// "user/login" 템플릿(HTML)만 반환
		return "user/login";
	}

	// roleSelection 페이지 요청
	@GetMapping("/roleSelection")
	public String roleSelection() {
		// "user/roleSelection.html" 템플릿을 반환
		return "user/roleSelection";
	}

	/**
     * 회원용 회원가입 화면 요청
     * URL 예: /user/join?role=ROLE_USER
     */
    @GetMapping("/join")
    public String userJoin(@RequestParam(name="role", required=false, defaultValue="ROLE_USER") String role,
                           Model model) {
        // 전달된 role 값을 뷰에 전달 (회원용이면 ROLE_USER)
        model.addAttribute("role", role);
        return "user/userJoin"; // templates/user/userJoin.html
    }
}
