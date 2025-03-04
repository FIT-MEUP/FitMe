package fitmeup.controller;


import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import fitmeup.service.FoodService;
import fitmeup.dto.FoodDTO;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FoodController {
	
	private final FoodService foodService;

    // 음식 검색 API (AJAX 요청 처리) : /food/search?query=김치 로 요청하면, JSON으로 검색 결과 반환
    @GetMapping("/foodsearch")
    public ResponseEntity<List<FoodDTO>> searchFood(@RequestParam(name="query", required = false ) String query) {
    	  if (query == null || query.isEmpty()) {
    	        return ResponseEntity.badRequest().build();  // 잘못된 요청 처리
    	    }
    	    List<FoodDTO> result = foodService.searchFood(query);
    	    return ResponseEntity.ok(result);
    }

}
