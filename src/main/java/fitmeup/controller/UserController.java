package fitmeup.controller;

import fitmeup.dto.UserDTO;
import fitmeup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    
    /**
     * 회원 가입 화면 요청 
     */
    @GetMapping("/join")
    public String join() {
        return "user/join";
    }
    
    /**
     * 이메일 중복 확인 (회원가입 시 사용)
     */
    @ResponseBody
    @PostMapping("/idCheck")
    public boolean idCheck(@RequestParam(name="email") String email) {
        boolean result = userService.existEmail(email);
        return result;
    }
    
    /**
     * 회원 가입 처리 
     */
    
    @PostMapping("/joinProc")
    public String joinProc(@ModelAttribute UserDTO userDTO) {
        log.info("회원 정보 : {}", userDTO.toString());
        boolean result = userService.joinProc(userDTO);
        return "redirect:/";
    }
    
    /**
     * 로그인 화면 요청 및 로그인 에러 처리
     */
    @GetMapping("/login")
    public String login(@RequestParam(name = "error", required = false) String error,
                        @RequestParam(name = "errMessage", required = false) String errMessage,
                        Model model) {
        model.addAttribute("error", error);
        model.addAttribute("errMessage", errMessage);
        return "/user/login";
    }
    
    // 나머지 마이페이지, 비밀번호 체크, 업데이트 등 기존 코드 유지
}
