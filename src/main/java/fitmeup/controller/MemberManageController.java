package fitmeup.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.qos.logback.core.model.Model;
import fitmeup.dto.MealDTO;

@Controller
public class MemberManageController {

    @GetMapping("/memberManage")
    public String memberManagePage(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "mealDate", required = false) String mealDate,
            Model model) {

        
        return ""



    
}
