package fitmeup.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fitmeup.entity.MealEntity;
import fitmeup.entity.UserEntity;

public interface MealRepository extends JpaRepository<MealEntity, Long> {
	
    // ✅ 특정 회원의 전체 식단 조회 (날짜순 정렬)
    List<MealEntity> findByUserOrderByMealDateDesc(UserEntity user);
    
    // ✅ 특정 날짜의 특정 회원의 식단 조회
    List<MealEntity> findByUserAndMealDate(UserEntity user, LocalDate mealDate);
    
}