package fitmeup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	@GetMapping("/login")
	public String login(@RequestParam(name = "error", required = false) String error,
			@RequestParam(name = "errMessage", required = false) String errMessage, Model model) {

		model.addAttribute("error", error);
		model.addAttribute("errMessage", errMessage); // 핸들러 처리에 의해 가져온 메시지

		return "/user/login";
	}

	/**
	 * 회원 가입 화면 요청
	 * 
	 * @return
	 */

	@GetMapping("/join")
	public String join() {
		return "user/join";
	}

}
