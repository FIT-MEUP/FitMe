package fitmeup.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fitmeup.entity.MealEntity;

public interface MealRepository extends JpaRepository<MealEntity, Long> {
	  // ✅ 특정 날짜의 식단 조회
    List<MealEntity> findByMealDate(LocalDate mealDate);
}