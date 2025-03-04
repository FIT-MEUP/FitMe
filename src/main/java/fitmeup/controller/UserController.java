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

    @GetMapping("/login")
    public String loginForm() {
        return "user/login"; // templates/user/login.html
    }

    @GetMapping("/roleSelection")
    public String roleSelection() {
        return "user/roleSelection";
    }

    @GetMapping("/join")
    public String userJoin(@RequestParam(name="role", required=false, defaultValue="User") String role,
                           Model model) {
        model.addAttribute("role", role);
        return "user/userJoin";
    }
}
