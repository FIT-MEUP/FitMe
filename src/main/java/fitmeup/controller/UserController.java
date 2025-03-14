package fitmeup.controller;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // 회원가입 폼
    @GetMapping("/userJoin")
    public String userJoin(@RequestParam(name="role", required=false, defaultValue="User") String role,
                           @RequestParam(name="error", required=false) String error, Model model) {
        model.addAttribute("role", role);
        model.addAttribute("error", error); // 📌 에러 메시지 추가
        return "user/userJoin";
    }

    // 회원가입 처리
    @PostMapping("/joinProc")
    public String joinProcess(@ModelAttribute UserDTO userDTO, RedirectAttributes redirectAttributes) {
        try {
            userService.joinProc(userDTO);
            return "redirect:/user/login";  // 회원가입 성공 시 로그인 페이지로 이동
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());  
            return "redirect:/user/userJoin"; // ❌ 실패 시 다시 회원가입 페이지로 이동
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
        
        @GetMapping("/findPassword")
        public String findPasswordForm() {
            return "user/findPassword"; // 📌 templates/user/findPassword.html 페이지 반환
        }
        
        
     // ✅ 개선된 코드 (더 깔끔하게 에러 메시지 전달)
        @PostMapping("/findPassword")
        public String findPassword(@RequestParam("userName") String userName,
                                   @RequestParam("userEmail") String userEmail,
                                   @RequestParam("userContact") String userContact,
                                   RedirectAttributes redirectAttributes) {
            boolean success = userService.verifyUserAndGenerateTempPassword(userName, userEmail, userContact);

            if (!success) {
                redirectAttributes.addFlashAttribute("error", "일치하지 않는 회원정보입니다!");
                return "redirect:/user/findPassword";  
            }

            // ✅ 성공 메시지 추가
            redirectAttributes.addFlashAttribute("successMessage", "임시 비밀번호가 이메일로 전송되었습니다. 로그인 후 반드시 변경하세요!");
            return "redirect:/user/login";  
        }

        // 📌 비밀번호 변경 페이지 이동
        @GetMapping("/changePassword")
        public String changePasswordForm() {
            return "user/changePassword"; // templates/user/changePassword.html
        }

        @PostMapping("/changePasswordProc")
        public String changePassword(@RequestParam("email") String email, 
                                     @RequestParam("currentPassword") String currentPassword, 
                                     @RequestParam("newPassword") String newPassword, 
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
        public String deleteAccountProc(@RequestParam("email") String email, 
                                    @RequestParam("password") String password, 
                                    RedirectAttributes redirectAttributes) {
            boolean success = userService.deleteUser(email, password, redirectAttributes);

            if (!success) {
                return "redirect:/user/deleteAccount";  
            }

            // 탈퇴 성공 시 로그아웃 처리 후 홈으로 이동
            SecurityContextHolder.clearContext();
            return "redirect:/user/logout";
        }

}

    

