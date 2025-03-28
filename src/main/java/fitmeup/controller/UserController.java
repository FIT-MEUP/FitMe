package fitmeup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.UserDTO;
import fitmeup.service.TrainerApplicationService;
import fitmeup.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private final UserService userService; // ✅ UserService 필드 추가 (자동 주입)
	private final TrainerApplicationService trainerApplicationService;
	private final SimpMessagingTemplate messagingTemplate;

	@GetMapping("/login")
	public String loginForm(HttpSession session, Model model) {
		// ✅ 로그인 페이지 처음 열 때 세션 초기화 (이전 에러 메시지 제거)
		if (session.getAttribute("errorMessage") != null) {
			model.addAttribute("errorMessage", session.getAttribute("errorMessage"));
			session.removeAttribute("errorMessage"); // 세션에서 삭제 (한 번만 표시되도록)
		}

		// ✅ 현재 로그인한 사용자의 이름 가져오기
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof LoginUserDetails) {
			LoginUserDetails userDetails = (LoginUserDetails) principal;
			model.addAttribute("loginName", userDetails.getUsername()); // 사용자 이름 추가
		}

		return "user/login"; // templates/user/login.html
	}

	@GetMapping("/roleSelection")
	public String roleSelection() {
		return "user/roleSelection"; // templates/user/roleSelection.html
	}

	// 회원가입 폼
	@GetMapping("/userJoin")
	public String userJoin(@RequestParam(name = "role", required = false, defaultValue = "User") String role,
			@RequestParam(name = "error", required = false) String error, Model model) {
		model.addAttribute("role", role);
		model.addAttribute("error", error); // 📌 에러 메시지 추가
		return "user/userJoin";
	}

	// 회원가입 처리
	@PostMapping("/joinProc")
	public String joinProcess(@ModelAttribute UserDTO userDTO, RedirectAttributes redirectAttributes) {
		String errorMessage = userService.joinProc(userDTO);

		if (errorMessage != null) {
			redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
			return "redirect:/user/userJoin"; // 실패 시 다시 회원가입 페이지로 이동
		}
		redirectAttributes.addFlashAttribute("successMessage", "회원가입이 정상적으로 완료되었습니다! 다시 로그인 해주세요.");
		return "redirect:/user/login"; // 성공 시 로그인 페이지로 이동
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
	public String findId(@RequestParam("userName") String userName, @RequestParam("userContact") String userContact,
			Model model) {
		String email = userService.findUserEmail(userName, userContact);

		if ("존재하지 않는 회원정보입니다.".equals(email)) {
			model.addAttribute("error", email); // 에러 메시지
		} else {
			model.addAttribute("email", email); // 정상 이메일
		}

		return "user/findId";
	}

	// ✅ 비밀번호 찾기 폼 페이지 (GET)
	@GetMapping("/findPassword")
	public String findPasswordForm() {
	    return "user/findPassword"; // 📌 templates/user/findPassword.html 반환
	}

	// ✅ 비밀번호 찾기 후 임시 비밀번호 표시 페이지로 이동 (POST)
	@PostMapping("/findPassword")
	public String findPassword(
	        @RequestParam("userName") String userName,
	        @RequestParam("userEmail") String userEmail,
	        @RequestParam("userContact") String userContact,
	        RedirectAttributes redirectAttributes,
	        Model model) {

	    // ✅ 임시 비밀번호 생성 및 검증
	    String tempPassword = userService.verifyUserAndGenerateTempPassword(userName, userEmail, userContact);

	    if (tempPassword == null) {
	        // 일치하는 회원 정보가 없는 경우
	        redirectAttributes.addFlashAttribute("error", "일치하지 않는 회원정보입니다!");
	        return "redirect:/user/findPassword";
	    }

	    // ✅ 임시 비밀번호를 모델에 추가하고 새로운 페이지로 이동
	    model.addAttribute("tempPassword", tempPassword);
	    return "user/tempPassword"; // 📌 templates/user/tempPassword.html 반환
	}


	// 📌 비밀번호 변경 페이지 이동
	@GetMapping("/changePassword")
	public String changePasswordForm() {
		return "user/changePassword"; // templates/user/changePassword.html
	}

	@PostMapping("/changePasswordProc")
	public String changePassword(@RequestParam("email") String email,
			@RequestParam("currentPassword") String currentPassword, @RequestParam("newPassword") String newPassword,
			RedirectAttributes redirectAttributes) {
		String errorMessage = userService.changePassword(email, currentPassword, newPassword);

		if (errorMessage != null) { // 실패한 경우
			redirectAttributes.addFlashAttribute("error", errorMessage);
			return "redirect:/user/changePassword";
		}

		// ✅ 비밀번호 변경 성공 시 성공 메시지 전달
		redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 정상적으로 변경되었습니다. 다시 로그인 해주세요!");

		return "redirect:/user/login";
	}

	@GetMapping("/deleteAccount")
	public String deleteAccount() {

		return "user/deleteAccount";
	}

	@PostMapping("/deleteAccountProc")
	public String deleteAccountProc(@RequestParam("email") String email, @RequestParam("password") String password,
			RedirectAttributes redirectAttributes) {
		boolean success = userService.deleteUser(email, password, redirectAttributes);

		if (!success) {
			return "redirect:/user/deleteAccount";
		}

		// 탈퇴 성공 시 로그아웃 처리 후 홈으로 이동
		SecurityContextHolder.clearContext();
		return "redirect:/user/logout";
	}

	// forceLogout 엔드포인트: 사용자 ID를 받아서 online 상태를 false로 설정
	// 강제 로그아웃 처리 메소드 추가
	@PostMapping("/forceLogout")
	@ResponseBody
	public ResponseEntity<String> forceLogout(@RequestParam("userId") Long userId) {
		userService.setOnline(userId, false);
		// STOMP로 로그아웃 브로드캐스팅
		messagingTemplate.convertAndSend("/topic/onlineStatus", "LOGOUT:" + userId);
		return ResponseEntity.ok("OK");
	}
}
