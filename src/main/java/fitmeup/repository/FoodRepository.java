package fitmeup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fitmeup.entity.FoodEntity;
import fitmeup.entity.MealEntity;

public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
	
    // 특정 식단(Meal)과 연결된 음식 조회
    List<FoodEntity> findByMeal(MealEntity meal);
    
    // 특정 식단에 속한 모든 음식 삭제
    void deleteByMeal(MealEntity meal);
    
    @Query("SELECT f FROM FoodEntity f LEFT JOIN FETCH f.meal WHERE f.foodName LIKE %:query%") // meal_id가 NULL이어도 가져옴
    List<FoodEntity> findByFoodNameContaining(@Param("query") String query);

}

/* void를 사용하는 이유
삭제 작업을 실행하지만, 반환값이 필요하지 않음

삭제 작업을 실행한 후 별도의 데이터 반환이 필요하지 않아 void를 사용함.
예를 들어, findByMeal(meal) 같은 조회 메서드는 데이터 목록을 반환해야 하지만, 삭제는 그저 실행만 하면 되기 때문!
*/
//List<FoodEntity> deleteByMeal(MealEntity meal);
