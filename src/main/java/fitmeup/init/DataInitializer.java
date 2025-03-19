package fitmeup.init;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import fitmeup.entity.AnnouncementEntity;
import fitmeup.entity.ChatEntity;
import fitmeup.entity.CommentEntity;
import fitmeup.entity.FoodEntity;
import fitmeup.entity.HealthDataEntity;
import fitmeup.entity.MealEntity;
import fitmeup.entity.PTSessionHistoryEntity;
import fitmeup.entity.PTSessionHistoryEntity.ChangeType;
import fitmeup.entity.ScheduleEntity;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerApplicationEntity.Status;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.entity.TrainerScheduleEntity;
import fitmeup.entity.UserEntity;
import fitmeup.entity.UserEntity.Gender;
import fitmeup.entity.UserEntity.Role;
import fitmeup.entity.WorkDataEntity;
import fitmeup.entity.WorkEntity;
import fitmeup.repository.AnnouncementRepository;
import fitmeup.repository.ChatRepository;
import fitmeup.repository.CommentRepository;
import fitmeup.repository.FoodRepository;
import fitmeup.repository.HealthDataRepository;
import fitmeup.repository.MealRepository;
import fitmeup.repository.PTSessionHistoryRepository;
import fitmeup.repository.ScheduleRepository;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerPhotoRepository;
import fitmeup.repository.TrainerRepository;
import fitmeup.repository.TrainerScheduleRepository;
import fitmeup.repository.UserRepository;
import fitmeup.repository.WorkDataRepository;
import fitmeup.repository.WorkRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final TrainerRepository trainerRepository;
	private final TrainerPhotoRepository trainerPhotoRepository;
	private final TrainerApplicationRepository trainerApplicationRepository;
	private final PTSessionHistoryRepository ptSessionHistoryRepository;
	private final ScheduleRepository scheduleRepository;
	private final WorkRepository workRepository;
	private final WorkDataRepository workDataRepository;
	private final HealthDataRepository healthDataRepository;
	private final AnnouncementRepository announcementRepository;
	private final MealRepository mealRepository;
	private final FoodRepository foodRepository;
	private final CommentRepository commentRepository;
	private final TrainerScheduleRepository trainerScheduleRepository;
	private final ChatRepository chatRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public void run(String... args) throws Exception {

		// 초기 관리자 계정 생성 (이미 존재하지 않을 경우)
		if (userRepository.findByUserEmail("admin@fitme.com").isEmpty()) {
			UserEntity admin = UserEntity.builder()
					.userName("Admin")
					.userEmail("admin@fitme.com")
					.password(passwordEncoder.encode("admin"))
					.userGender(UserEntity.Gender.Male)
					.userBirthdate(LocalDate.of(1988, 1, 1))
					.userContact("010-9999-9999")
					.role(UserEntity.Role.Admin)
					.isOnline(false)
					.build();
			userRepository.save(admin);

			produceDummy(admin);
		}
	}

	public void produceDummy(UserEntity admin) throws Exception {
		Random random = new Random();

		// 기존 데이터 삭제 (테스트/개발 환경)
		// (필요한 경우 아래 주석 해제)
		// commentRepository.deleteAll();
		// foodRepository.deleteAll();
		// mealRepository.deleteAll();
		// workDataRepository.deleteAll();
		// announcementRepository.deleteAll();
		// chatRepository.deleteAll();
		// scheduleRepository.deleteAll();
		// ptSessionHistoryRepository.deleteAll();
		// trainerApplicationRepository.deleteAll();
		// trainerScheduleRepository.deleteAll();
		// trainerPhotoRepository.deleteAll();
		// trainerRepository.deleteAll();
		// healthDataRepository.deleteAll();
		// workRepository.deleteAll();
		// userRepository.deleteAll();

		// 2. Trainer 계정 및 관련 데이터 생성 (5명)
		for (int i = 1; i <= 5; i++) {
			String trainerEmail = "trainer" + i + "@fitme.com";
			UserEntity trainerUser = UserEntity.builder()
					.userName("Trainer" + i)
					.userEmail(trainerEmail)
					.password(passwordEncoder.encode("trainer" + i + "!"))
					.userGender(Gender.Male)
					.userBirthdate(LocalDate.of(1985, 1, i))
					.userContact("010-1111-" + String.format("%02d", i))
					.role(Role.Trainer)
					.isOnline(false)
					.build();
			trainerUser = userRepository.save(trainerUser);

			TrainerEntity trainer = TrainerEntity.builder()
					.user(trainerUser)
					.specialization("Specialization" + i)
					.experience(5 + i)
					.fee(new BigDecimal(50 + i * 10))
					.bio("Bio for Trainer" + i)
					.shortIntro("Short intro for Trainer" + i)
					.build();
			trainer = trainerRepository.save(trainer);

			TrainerPhotoEntity photo = TrainerPhotoEntity.builder()
					.trainer(trainer)
					.photoUrl("http://dummyimage.com/300x300/000/fff&text=Trainer" + i)
					.build();
			trainerPhotoRepository.save(photo);

			// Approved 상태의 TrainerApplication 10건 생성
			// 이 중 첫 번째로 생성된 클라이언트를 chatReceiver로 저장
			UserEntity chatReceiver = null;
			for (int j = 1; j <= 10; j++) {
				String clientEmail = "client" + i + "_approved_" + j + "@fitme.com";
				UserEntity client = UserEntity.builder()
						.userName("Client" + i + "_Approved_" + j)
						.userEmail(clientEmail)
						.password(passwordEncoder.encode("client" + i + "_approved_" + j + "!"))
						.userGender(Gender.Female)
						.userBirthdate(LocalDate.of(1990, 5, Math.min(j, 28)))
						.userContact("010-2222-" + String.format("%03d", i * 100 + j))
						.role(Role.User)
						.isOnline(false)
						.build();
				client = userRepository.save(client);

				// 첫 번째 Approved 신청을 chatReceiver로 저장
				if (j == 1) {
					chatReceiver = client;
				}

				TrainerApplicationEntity application = TrainerApplicationEntity.builder()
						.user(client)
						.trainer(trainer)
						.status(Status.Approved)
						.appliedAt(LocalDateTime.now().minusDays(10 + j))
						.responseAt(LocalDateTime.now().minusDays(5 + j))
						.build();
				trainerApplicationRepository.save(application);
			}

			// Pending 상태의 TrainerApplication 10건 생성
			for (int j = 1; j <= 10; j++) {
				String clientEmail = "client" + i + "_pending_" + j + "@fitme.com";
				UserEntity client = UserEntity.builder()
						.userName("Client" + i + "_Pending_" + j)
						.userEmail(clientEmail)
						.password(passwordEncoder.encode("client" + i + "_pending_" + j + "!"))
						.userGender(Gender.Female)
						.userBirthdate(LocalDate.of(1990, 5, Math.min(j, 28)))
						.userContact("010-3333-" + String.format("%03d", i * 100 + j))
						.role(Role.User)
						.isOnline(false)
						.build();
				client = userRepository.save(client);

				TrainerApplicationEntity application = TrainerApplicationEntity.builder()
						.user(client)
						.trainer(trainer)
						.status(Status.Pending)
						.appliedAt(LocalDateTime.now().minusDays(15 + j))
						.responseAt(null)
						.build();
				trainerApplicationRepository.save(application);
			}

			// Rejected 상태의 TrainerApplication 10건 생성
			for (int j = 1; j <= 10; j++) {
				String clientEmail = "client" + i + "_rejected_" + j + "@fitme.com";
				UserEntity client = UserEntity.builder()
						.userName("Client" + i + "_Rejected_" + j)
						.userEmail(clientEmail)
						.password(passwordEncoder.encode("client" + i + "_rejected_" + j + "!"))
						.userGender(Gender.Female)
						.userBirthdate(LocalDate.of(1990, 5, Math.min(j, 28)))
						.userContact("010-4444-" + String.format("%03d", i * 100 + j))
						.role(Role.User)
						.isOnline(false)
						.build();
				client = userRepository.save(client);

				TrainerApplicationEntity application = TrainerApplicationEntity.builder()
						.user(client)
						.trainer(trainer)
						.status(Status.Rejected)
						.appliedAt(LocalDateTime.now().minusDays(20 + j))
						.responseAt(LocalDateTime.now().minusDays(10 + j))
						.build();
				trainerApplicationRepository.save(application);
			}

			// PTSessionHistory: 각 트레이너에 대해 Added와 Deducted 각각 10건씩 생성
			for (int j = 1; j <= 10; j++) {
				PTSessionHistoryEntity addedHistory = PTSessionHistoryEntity.builder()
						.user(trainerUser)
						.changeType(ChangeType.Added)
						.changeAmount(5L + j)
						.changeDate(LocalDateTime.now().minusDays(2 + j))
						.reason("Added PT bonus " + j + " for Trainer" + i)
						.build();
				ptSessionHistoryRepository.save(addedHistory);
			}
			for (int j = 1; j <= 10; j++) {
				PTSessionHistoryEntity deductedHistory = PTSessionHistoryEntity.builder()
						.user(trainerUser)
						.changeType(ChangeType.Deducted)
						.changeAmount(3L + j)
						.changeDate(LocalDateTime.now().minusDays(1 + j))
						.reason("Deducted PT session " + j + " for Trainer" + i)
						.build();
				ptSessionHistoryRepository.save(deductedHistory);
			}

			// Schedule (트레이너 자신의 일정) 생성
			ScheduleEntity schedule = ScheduleEntity.builder()
					.trainer(trainer)
					.user(trainerUser)
					.status(ScheduleEntity.Status.Approved)
					.attendanceStatus(ScheduleEntity.AttendanceStatus.Present)
					.sessionDeducted(false)
					.startTime(LocalDateTime.now().plusHours(1))
					.endTime(LocalDateTime.now().plusHours(2))
					.build();
			scheduleRepository.save(schedule);

			// Work (운동 기록) 생성
			WorkEntity work = WorkEntity.builder()
					.user(trainerUser)
					.part("Chest")
					.exercise("Bench Press " + i)
					.sets(3)
					.reps(10)
					.weight(50 + i * 5)
					.workoutDate(LocalDate.now().minusDays(1))
					.build();
			work = workRepository.save(work);

			// WorkData (운동 데이터) 생성 – WorkDataEntity의 필드명이 "workout"임
			if (work != null && work.getWorkoutId() != null) {
				WorkDataEntity workData = WorkDataEntity.builder()
						.workout(work)
						.originalFileName(String.format("work%d_orig.mp4", i))
						.savedFileName(String.format("work%d_saved.mp4", i))
						.build();
				workDataRepository.save(workData);
			} else {
				System.out.println("Work entity is null or not persisted for Trainer" + i);
			}

			// HealthData (건강 데이터) 생성
			HealthDataEntity healthData = HealthDataEntity.builder()
					.user(trainerUser)
					.weight(BigDecimal.valueOf(70 + i))
					.muscleMass(BigDecimal.valueOf(30 + i))
					.fatMass(BigDecimal.valueOf(15 - i * 0.5))
					.height(BigDecimal.valueOf(170))
					.bmi(BigDecimal.valueOf(22.5 + i * 0.2))
					.basalMetabolicRate(BigDecimal.valueOf(1500 + i * 50))
					.recordDate(LocalDate.now().minusDays(1))
					.build();
			healthDataRepository.save(healthData);

			// TrainerSchedule (트레이너 일정) 생성
			TrainerScheduleEntity trainerSchedule = TrainerScheduleEntity.builder()
					.trainer(trainer)
					.startTime(LocalDateTime.now().plusHours(2))
					.endTime(LocalDateTime.now().plusHours(4))
					.build();
			trainerScheduleRepository.save(trainerSchedule);

			// Chat (채팅) 생성
			// 채팅의 sender는 트레이너(Trainer 역할의 user), receiver는 Approved 신청에서 선택된 클라이언트(일반 User)
			if (chatReceiver != null && !chatReceiver.getUserId().equals(trainerUser.getUserId())) {
				ChatEntity chat = ChatEntity.builder()
						.sender(trainerUser)      // 트레이너 계정의 user
						.receiver(chatReceiver)   // Approved 상태의 신청을 한 클라이언트
						.message("Hello, this is Trainer " + trainerUser.getUserName() + " sending a chat!")
						.sentAt(LocalDateTime.now().minusMinutes(30))
						.isRead(false)
						.originalFileName("chat" + i + ".jpg")
						.savedFileName("chat" + i + "_saved.jpg")
						.fileType("image")
						.fileUrl("http://dummyfile.com/chat" + i + ".jpg")
						.build();
				chatRepository.save(chat);
			} else {
				System.out.println("Approved application not found or sender equals receiver for Trainer" + i);
			}
		}

		// 3. Pending Trainer 계정 생성 (5명)
		for (int i = 1; i <= 5; i++) {
			String email = "pending" + i + "@fitme.com";
			UserEntity pending = UserEntity.builder()
					.userName("PendingTrainer" + i)
					.userEmail(email)
					.password(passwordEncoder.encode("pending" + i + "!"))
					.userGender(Gender.Female)
					.userBirthdate(LocalDate.of(1992, 2, i))
					.userContact("010-3333-" + String.format("%02d", i))
					.role(Role.PendingTrainer)
					.isOnline(false)
					.build();
			userRepository.save(pending);
		}

		// 4. 일반 User 계정 생성 (5명)
		for (int i = 1; i <= 5; i++) {
			String email = "user" + i + "@fitme.com";
			UserEntity user = UserEntity.builder()
					.userName("User" + i)
					.userEmail(email)
					.password(passwordEncoder.encode("user" + i + "!"))
					.userGender(Gender.Male)
					.userBirthdate(LocalDate.of(2000, 3, i))
					.userContact("010-4444-" + String.format("%02d", i))
					.role(Role.User)
					.isOnline(false)
					.build();
			userRepository.save(user);
		}

		// 5. Announcement (공지사항) 생성 (5건, 관리자 작성)
		for (int i = 1; i <= 5; i++) {
			AnnouncementEntity announcement = AnnouncementEntity.builder()
					.user(admin)
					.content("Announcement " + i + ": New schedule updates.")
					.createdAt(LocalDateTime.now().minusHours(i))
					.build();
			announcementRepository.save(announcement);
		}

		// 5-2. Announcement (공지사항) 생성 - 트레이너 작성
		// 모든 트레이너 계정을 조회하여, 각 트레이너가 작성한 공지사항을 생성합니다.
		List<TrainerEntity> trainers = trainerRepository.findAll();
		for (TrainerEntity trainer : trainers) {
			AnnouncementEntity trainerAnnouncement = AnnouncementEntity.builder()
					.user(trainer.getUser())
					.content("Trainer " + trainer.getUser().getUserName() + " Announcement: Check out my new schedule!")
					.createdAt(LocalDateTime.now().minusMinutes(10))
					.build();
			announcementRepository.save(trainerAnnouncement);
		}

		// 6. Food 테이블에 INSERT 구문 실행
		List<String> queries;
		try {
			queries = Files.readAllLines(Paths.get("src/main/resources/food_insert.sql"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// 읽어온 각 SQL 구문을 native query로 실행
		for (String query : queries) {
			query = query.trim();
			// 빈 줄이나 주석은 건너뜁니다.
			if (query.isEmpty() || query.startsWith("--") || query.startsWith("//") || query.startsWith("/*")) {
				continue;
			}
			try {
				entityManager.createNativeQuery(query).executeUpdate();
			} catch (Exception e) {
				System.out.println("Error executing query: " + query);
				e.printStackTrace();
			}
		}
		System.out.println("Food INSERT queries executed successfully.");

		// 7. Meal 및 Food 더미 데이터 생성
		List<FoodEntity> foodList = foodRepository.findAll();
		String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
		for (int i = 1; i <= 5; i++) {
			if (foodList.size() < 3) break;
			FoodEntity f1 = foodList.get(random.nextInt(foodList.size()));
			FoodEntity f2 = foodList.get(random.nextInt(foodList.size()));
			FoodEntity f3 = foodList.get(random.nextInt(foodList.size()));

			double totalCalories = f1.getCalories() + f2.getCalories() + f3.getCalories();
			double totalCarbs = f1.getCarbs() + f2.getCarbs() + f3.getCarbs();
			double totalProtein = f1.getProtein() + f2.getProtein() + f3.getProtein();
			double totalFat = f1.getFat() + f2.getFat() + f3.getFat();

			String mealType = mealTypes[random.nextInt(mealTypes.length)];

			MealEntity meal = MealEntity.builder()
					.user(admin)
					.mealDate(LocalDate.now().minusDays(i))
					.mealType(mealType)
					.totalCalories(totalCalories)
					.totalCarbs(totalCarbs)
					.totalProtein(totalProtein)
					.totalFat(totalFat)
					.originalFileName("meal" + i + "_orig.jpg")
					.savedFileName("meal" + i + "_saved.jpg")
					.build();
			mealRepository.save(meal);
		}

		// 8. Comment (댓글) 생성
		List<WorkEntity> works = workRepository.findAll();
		if (!works.isEmpty()) {
			for (int i = 1; i <= 5; i++) {
				WorkEntity work = works.get(random.nextInt(works.size()));
				CommentEntity comment = CommentEntity.builder()
						.user(admin)
						.workout(work)
						.content("Great workout! Comment " + i)
						.createdAt(LocalDateTime.now().minusMinutes(10 * i))
						.build();
				commentRepository.save(comment);
			}
		}
		List<MealEntity> meals = mealRepository.findAll();
		if (!meals.isEmpty()) {
			for (int i = 1; i <= 5; i++) {
				MealEntity meal = meals.get(random.nextInt(meals.size()));
				CommentEntity comment = CommentEntity.builder()
						.user(admin)
						.meal(meal)
						.content("Looks delicious! Comment " + i)
						.createdAt(LocalDateTime.now().minusMinutes(5 * i))
						.build();
				commentRepository.save(comment);
			}
		}

		System.out.println("모든 ddl 테이블에 대해 더미 데이터가 초기화되었습니다.");
	}
}
