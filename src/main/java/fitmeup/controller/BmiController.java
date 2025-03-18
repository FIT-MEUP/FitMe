package fitmeup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BmiController {

    @GetMapping("/BMI")
    public String showBmiDiagnosis() {
        return "BMI"; // 
    }
}
