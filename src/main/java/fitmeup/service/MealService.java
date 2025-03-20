package fitmeup.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fitmeup.dto.FoodDTO;
import fitmeup.dto.MealDTO;
import fitmeup.entity.FoodEntity;
import fitmeup.entity.MealEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.FoodRepository;
import fitmeup.repository.MealRepository;
import fitmeup.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class MealService {

	@Autowired
    private MealRepository mealRepository;
	@Autowired
    private FoodRepository foodRepository;
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private TrainerApplicationService trainerApplicationService;
	@Autowired
    public MealService(MealRepository mealRepository, FoodRepository foodRepository) {
        this.mealRepository = mealRepository;
        this.foodRepository = foodRepository;
    }
    
    @Value("${upload.meal.path}")
    private String mealUploadDir;  	

 // 특정 회원의 특정 날짜 식단 조회 (음식 목록 포함) / 트레이너는 승인된 회원것만 
    public List<MealDTO> getMealsByUserAndDate(Long userId, LocalDate mealDate, Long loginUserId, String role) {
        
    	if (userId == null) {
            userId = loginUserId; // 본인 ID로 설정
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 본인만 조회 가능
        if (!"Trainer".equals(role) && !Objects.equals(loginUserId, userId)) {
            throw new RuntimeException("본인의 식단만 조회할 수 있습니다.");
        }

        // 트레이너는 승인된 회원만 조회 가능
        if ("Trainer".equals(role) && !Objects.equals(loginUserId, userId)) {
            List<Long> approvedUserIds = trainerApplicationService.getTrainerMembers(loginUserId)
                    .stream()
                    .map(UserEntity::getUserId)
                    .toList();

            if (!approvedUserIds.contains(userId)) {
                throw new RuntimeException("🚨 이 회원의 식단을 조회할 권한이 없습니다!");
            }
        }

        List<MealEntity> meals = mealRepository.findByUserAndMealDateWithFood(user, mealDate);

        return meals.stream()
                .map(meal -> {
                    List<FoodDTO> foodList = meal.getFoodList().stream()
                            .map(FoodDTO::fromEntity)
                            .collect(Collectors.toList());

                    return MealDTO.fromEntity(meal, foodList);
                })
                .collect(Collectors.toList());
        
    }

    // 특정 회원의 전체 식단 조회 (날짜 순 정렬)
    public List<MealDTO> getMealsByUser(Long userId, Long loginUserId, String role) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        
        // 회원인 경우 본인 식단만 조회 가능
        if (!"Trainer".equals(role) && !userId.equals(loginUserId)) {
            throw new RuntimeException("본인의 식단만 조회할 수 있습니다.");
        }

        // 트레이너인 경우 승인된 회원만 조회 가능
        if ("Trainer".equals(role) && !trainerApplicationService.isTrainerOfUser(loginUserId, userId)) {
            throw new RuntimeException("이 회원의 식단을 조회할 권한이 없습니다.");
        }

        return mealRepository.findByUserOrderByMealDateDesc(user)
                .stream()
                .map(meal -> {
                    List<FoodDTO> foodList = foodRepository.findByMeal(meal)
                            .stream()
                            .map(FoodDTO::fromEntity)
                            .collect(Collectors.toList());

                    return MealDTO.fromEntity(meal, foodList);
                })
                .collect(Collectors.toList());
    }

 // 새로운 식단 저장 (음식도 함께 저장)
    @Transactional
    public MealDTO saveMeal(MealDTO mealDTO, Long loginUserId, String role) {
    	
        if ("Trainer".equals(role)) {
            throw new RuntimeException("트레이너는 식단을 추가할 수 없습니다.");
        }

        if (!mealDTO.getUserId().equals(loginUserId)) {
            throw new RuntimeException("본인의 식단만 추가할 수 있습니다.");
        }
        
        UserEntity userEntity = userRepository.findById(mealDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음"));

        // 1. MealEntity 생성
        MealEntity meal = mealDTO.toEntity(userEntity);
        meal.setMealType(mealDTO.getMealType()); // ✅ mealType을 먼저 설정

        // 2. MealEntity 저장
        MealEntity savedMeal = mealRepository.save(meal); // ✅ mealType이 설정된 상태에서 저장

        // 3. FoodEntity 저장 (음식 리스트가 있으면 추가)
        if (mealDTO.getFoodList() != null && !mealDTO.getFoodList().isEmpty()) {
            List<FoodEntity> foodEntities = mealDTO.getFoodList().stream()
                    .map(foodDTO -> foodDTO.toEntity(savedMeal)) // MealEntity와 연결
                    .collect(Collectors.toList());

            foodRepository.saveAll(foodEntities);
        }

        // 저장된 Meal과 연관된 Food 리스트 가져오기
        List<FoodDTO> savedFoodList = foodRepository.findByMeal(savedMeal)
                .stream()
                .map(FoodDTO::fromEntity)
                .collect(Collectors.toList());

        //수정된 fromEntity 호출 (음식 리스트 포함)
        return MealDTO.fromEntity(savedMeal, savedFoodList);
    }

    
 // 4. 특정 식단 삭제 (음식도 함께 삭제)
    @Transactional
    public void deleteMeal(Long mealId, Long loginUserId, String role) {
        MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단을 찾을 수 없습니다."));
        
        if ("Trainer".equals(role)) {
            throw new RuntimeException("트레이너는 식단을 삭제할 수 없습니다.");
        }

        if (!meal.getUser().getUserId().equals(loginUserId)) {
            throw new RuntimeException("본인의 식단만 삭제할 수 있습니다.");
        }

        // 먼저 연결된 음식 데이터 삭제
        foodRepository.deleteByMeal(meal);

        // 식단 삭제
        mealRepository.delete(meal);
    }

    
    // 식단 수정 
    @Transactional
    public void updateMeal(Long mealId, String mealType, double totalCalories, double totalCarbs, double totalProtein,
            double totalFat, List<Long> foodIds, MultipartFile file, Long loginUserId, String role) {
        MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단을 찾을 수 없습니다."));
        
        if ("Trainer".equals(role)) {
            throw new RuntimeException("트레이너는 식단을 수정할 수 없습니다.");
        }

        if (!meal.getUser().getUserId().equals(loginUserId)) {
            throw new RuntimeException("본인의 식단만 수정할 수 있습니다.");
        }

        meal.setMealType(mealType);
        meal.setTotalCalories(totalCalories);
        meal.setTotalCarbs(totalCarbs);
        meal.setTotalProtein(totalProtein);
        meal.setTotalFat(totalFat);

        if (foodIds != null) {
            List<FoodEntity> foodEntities = foodRepository.findAllById(foodIds);
            meal.setFoodList(foodEntities);
        }

        //  파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path destinationPath = Paths.get(mealUploadDir, fileName);
                Files.copy(file.getInputStream(), destinationPath);
                meal.setSavedFileName(fileName); // 🔹 저장된 파일명 설정
            } catch (Exception e) {
                throw new RuntimeException("파일 저장 중 오류 발생!", e);
            }
        }

        mealRepository.save(meal);
    }

    
    public MealDTO getMealById(Long mealId, Long loginUserId, String role) {
    	
    	 MealEntity meal = mealRepository.findById(mealId)
    	            .orElseThrow(() -> new IllegalArgumentException("해당 식단을 찾을 수 없습니다."));

    	    Long ownerId = meal.getUser().getUserId();

    	    // 회원인 경우 본인 식단만 조회 가능
    	    if (!"Trainer".equals(role) && !ownerId.equals(loginUserId)) {
    	        throw new RuntimeException("본인의 식단만 조회할 수 있습니다.");
    	    }

    	    // 트레이너인 경우 승인된 회원만 조회 가능
    	    if ("Trainer".equals(role) && !trainerApplicationService.isTrainerOfUser(loginUserId, ownerId)) {
    	        throw new RuntimeException("이 회원의 식단을 조회할 권한이 없습니다.");
    	    }

    	    List<FoodDTO> foodList = meal.getFoodList().stream()
    	            .map(FoodDTO::fromEntity)
    	            .collect(Collectors.toList());

    	    return MealDTO.fromEntity(meal, foodList);
    }

    // 파일 처리 
    @Transactional
    public void updateMealImage(Long mealId, MultipartFile file, Long loginUserId, String role) {

    	MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단을 찾을 수 없습니다."));

        Long ownerId = meal.getUser().getUserId();

        // 트레이너는 이미지 업로드 불가
        if ("Trainer".equals(role)) {
            throw new RuntimeException("트레이너는 식단 이미지를 수정할 수 없습니다.");
        }

        // 회원은 본인 식단만 수정 가능
        if (!ownerId.equals(loginUserId)) {
            throw new RuntimeException("본인의 식단만 수정할 수 있습니다.");
        }

        // 파일 확장자 검사
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || (!originalFileName.endsWith(".jpg") && !originalFileName.endsWith(".png"))) {
            throw new RuntimeException("지원하는 파일 형식은 JPG, PNG만 가능합니다.");
        }

        try {
            // 고유한 파일명 생성 (UUID 사용)
            String savedFileName = UUID.randomUUID() + "_" + originalFileName;

            // 파일 저장
            Path destinationPath = Paths.get(mealUploadDir, savedFileName);
            Files.copy(file.getInputStream(), destinationPath);

            // DB 업데이트
            meal.setSavedFileName(savedFileName);
            meal.setOriginalFileName(originalFileName);
            mealRepository.save(meal);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생!", e);
        }
    }
    
    public List<String> getMealDatesForMonth(Long userId, int year, int month, Long loginUserId, String role) {
        if (userId == null) {
            userId = loginUserId;
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 일반 회원: 본인만
        if (!"Trainer".equals(role) && !Objects.equals(loginUserId, userId)) {
            throw new RuntimeException("본인의 식단만 조회할 수 있습니다.");
        }

        // 트레이너: 승인된 회원만
        if ("Trainer".equals(role) && !Objects.equals(loginUserId, userId)) {
            List<Long> approvedUserIds = trainerApplicationService.getTrainerMembers(loginUserId)
                    .stream().map(UserEntity::getUserId).toList();
            if (!approvedUserIds.contains(userId)) {
                throw new RuntimeException("🚨 이 회원의 식단을 조회할 권한이 없습니다!");
            }
        }

        List<LocalDate> dates = mealRepository.findMealDatesByUserAndMonth(user, year, month);

        // 문자열로 변환해서 리턴 (["2025-03-01", ...])
        return dates.stream().map(LocalDate::toString).toList();
    }

}
