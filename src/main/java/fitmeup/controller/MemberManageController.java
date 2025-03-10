package fitmeup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.qos.logback.core.model.Model;

@Controller
public class MemberManageController {

    @GetMapping("/memberManage")
    public String memberManagePage(
            @RequestParam(name = "userId", required = false) Long userId,
            Model model) {

        
        return "";
            }



    
}
