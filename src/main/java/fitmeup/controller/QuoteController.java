package fitmeup.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fitmeup.service.QuoteApiService;

@RestController
@RequestMapping("/api")
public class QuoteController {

    @Autowired
    private QuoteApiService quoteApiService;
    
    // 실제 서비스 전까진 명언 API 사용X. 사용시 요금 청구됨.
    // @GetMapping("/quote")
    public Map<String, String> getQuote() {
        String quote = quoteApiService.getMotivationalQuote();
        Map<String, String> response = new HashMap<>();
        response.put("quote", quote);
        return response;
    }
}
