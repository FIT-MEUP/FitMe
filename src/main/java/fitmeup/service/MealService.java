package fitmeup.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fitmeup.dto.MealDTO;
import fitmeup.entity.MealEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.MealRepository;
import fitmeup.repository.UserRepository;

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ 특정 날짜의 식단 조회
    public List<MealDTO> getMealsByDate(LocalDate mealDate) {
        return mealRepository.findByMealDate(mealDate)
                .stream()
                .map(MealDTO::fromEntity) // Entity → DTO 변환 메서드 사용
                .collect(Collectors.toList());
    }

    // ✅ 새로운 식단 저장
    public MealDTO saveMeal(MealDTO mealDTO) {
        UserEntity userEntity = userRepository.findById(mealDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음"));

        MealEntity meal = mealDTO.toEntity(userEntity); // DTO → Entity 변환
        MealEntity savedMeal = mealRepository.save(meal);
        return MealDTO.fromEntity(savedMeal); // Entity → DTO 변환
    }
}
