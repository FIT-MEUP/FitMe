package fitmeup.init;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Set;
import java.util.stream.Collectors;
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
	public void run(String... args) throws Exception {
		// 기존 데이터 삭제 및 AUTO_INCREMENT 리셋 등 초기화 작업 (환경에 맞게 구현)
		// 예: entityManager.createNativeQuery("TRUNCATE TABLE user").executeUpdate();
		// (이 부분은 생략)

		UserEntity admin = new UserEntity();

		if (userRepository.findByUserEmail("admin@fitme.com").isEmpty()) {
			admin = UserEntity.builder()
					.userName("Admin")
					.userEmail("admin@fitme.com")
					.password(passwordEncoder.encode("admin"))
					.userGender(UserEntity.Gender.Male)
					.userBirthdate(LocalDate.of(1988, 1, 1))
					.userContact("010-9999-9999")
					.role(UserEntity.Role.Admin)
					.build();
			userRepository.save(admin);

			produceDummy(admin);

		}
	}

	@Transactional
	public void produceDummy(UserEntity admin) {
		Random random = new Random();

// (2) Trainer 계정 및 관련 데이터 생성 (5명)
		for (int i = 1; i <= 5; i++) {
			String trainerEmail = "trainer" + i + "@fitme.com";
			UserEntity trainerUser = UserEntity.builder()
					.userName("Trainer" + i)
					.userEmail(trainerEmail)
					.password(passwordEncoder.encode("trainer" + i + "!"))
					.userGender(Gender.Male)
					.userBirthdate(LocalDate.of(1985, 1, Math.min(i, 28)))
					.userContact("010-1111-" + String.format("%02d", i))
					.role(Role.Trainer)
					.isOnline(true)
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

			// TrainerApplication 생성 (Approved, Pending, Rejected 각각 10건씩)
			for (int j = 1; j <= 10; j++) {
				// Approved
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

				TrainerApplicationEntity appApproved = TrainerApplicationEntity.builder()
						.user(client)
						.trainer(trainer)
						.status(Status.Approved)
						.appliedAt(LocalDateTime.now().minusDays(10 + j))
						.responseAt(LocalDateTime.now().minusDays(5 + j))
						.build();
				trainerApplicationRepository.save(appApproved);
			}
			for (int j = 1; j <= 10; j++) {
				// Pending
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

				TrainerApplicationEntity appPending = TrainerApplicationEntity.builder()
						.user(client)
						.trainer(trainer)
						.status(Status.Pending)
						.appliedAt(LocalDateTime.now().minusDays(15 + j))
						.responseAt(null)
						.build();
				trainerApplicationRepository.save(appPending);
			}
			for (int j = 1; j <= 10; j++) {
				// Rejected
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

				TrainerApplicationEntity appRejected = TrainerApplicationEntity.builder()
						.user(client)
						.trainer(trainer)
						.status(Status.Rejected)
						.appliedAt(LocalDateTime.now().minusDays(20 + j))
						.responseAt(LocalDateTime.now().minusDays(10 + j))
						.build();
				trainerApplicationRepository.save(appRejected);
			}

			// PTSessionHistory: Approved 상태의 TrainerApplication에 해당하는 경우에만 생성
			List<TrainerApplicationEntity> approvedApps = trainerApplicationRepository
					.findByTrainerTrainerId(trainer.getTrainerId())
					.stream()
					.filter(app -> app.getStatus() == Status.Approved)
					.collect(Collectors.toList());
			if (!approvedApps.isEmpty()) {
				for (int j = 1; j <= 10; j++) {
					PTSessionHistoryEntity addedHistory = PTSessionHistoryEntity.builder()
							.user(trainerUser)
							.changeType(ChangeType.Added)
							.changeAmount(5L + j)
							.changeDate(LocalDateTime.now().minusDays(2 + j))
							.reason("Added PT bonus " + j + " for Trainer userId " + trainerUser.getUserId())
							.build();
					ptSessionHistoryRepository.save(addedHistory);
				}
				for (int j = 1; j <= 10; j++) {
					PTSessionHistoryEntity deductedHistory = PTSessionHistoryEntity.builder()
							.user(trainerUser)
							.changeType(ChangeType.Deducted)
							.changeAmount(3L + j)
							.changeDate(LocalDateTime.now().minusDays(1 + j))
							.reason("Deducted PT session " + j + " for Trainer userId " + trainerUser.getUserId())
							.build();
					ptSessionHistoryRepository.save(deductedHistory);
				}
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

			// (Work & WorkData) 생성: Meal과 Work는 참조하는 User가 일반 User(Role.User)여야 하고,
			// 해당 User는 TrainerApplication에서 Approved 상태여야 함.
			// TrainerApplication에서 Approved 상태인 클라이언트 User ID들을 수집
			Set<Long> approvedClientIds = trainerApplicationRepository.findAll()
					.stream()
					.filter(app -> app.getStatus() == Status.Approved)
					.map(app -> app.getUser().getUserId())
					.collect(Collectors.toSet());
			List<UserEntity> approvedClients = userRepository.findAllById(approvedClientIds);
			if (!approvedClients.isEmpty()) {
				// Work (운동 기록) 생성: 일반 User 중 랜덤 선택
				UserEntity workoutUser = approvedClients.get(random.nextInt(approvedClients.size()));
				WorkEntity work = WorkEntity.builder()
						.user(workoutUser)
						.part("Chest")
						.exercise("Bench Press " + i)
						.sets(3)
						.reps(10)
						.weight(50 + i * 5)
						.workoutDate(LocalDate.now().minusDays(1))
						.build();
				work = workRepository.save(work);

				WorkDataEntity workData = WorkDataEntity.builder()
						.workout(work)
						.originalFileName(String.format("work%d_orig.mp4", i))
						.savedFileName(String.format("work%d_saved.mp4", i))
						.build();
				workDataRepository.save(workData);
			} else {
				System.out.println("Approved 일반 User 계정이 없습니다. Work 데이터를 생성하지 않습니다.");
			}

			// HealthData (건강 데이터) 생성 (트레이너 기준)
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

			// Chat 생성: Approved된 TrainerApplication에 해당하는 경우, 트레이너와 클라이언트 간의 채팅 생성
			List<TrainerApplicationEntity> allApps = trainerApplicationRepository.findAll();
			approvedApps = null;
			approvedApps = allApps.stream()
					.filter(app -> app.getStatus() == Status.Approved)
					.collect(Collectors.toList());
			for (TrainerApplicationEntity app : approvedApps) {
				trainerUser = app.getTrainer().getUser();
				UserEntity client = app.getUser();
				// Approved 상태인 경우에만 채팅 생성 (0~10개)
				int numMessages = random.nextInt(11); // 0~10개
				for (int k = 0; k < numMessages; k++) {
					// 번갈아 가며 메시지 발신: 짝수번째 메시지는 트레이너, 홀수번째 메시지는 클라이언트가 발신
					UserEntity sender = (k % 2 == 0) ? trainerUser : client;
					UserEntity receiver = (k % 2 == 0) ? client : trainerUser;
					ChatEntity chat;
					boolean isTextMessage = random.nextBoolean();
					if (isTextMessage) {
						chat = ChatEntity.builder()
								.sender(sender)
								.receiver(receiver)
								.message("Chat message " + (k + 1) + " between Trainer userId "
										+ trainerUser.getUserId() + " and Client userId " + client.getUserId())
								.sentAt(LocalDateTime.now().minusMinutes(random.nextInt(60)))
								.isRead(random.nextBoolean())
								.originalFileName(null)
								.savedFileName(null)
								.fileType(null)
								.fileUrl(null)
								.build();
					} else {
						chat = ChatEntity.builder()
								.sender(sender)
								.receiver(receiver)
								.message(null)
								.sentAt(LocalDateTime.now().minusMinutes(random.nextInt(60)))
								.isRead(random.nextBoolean())
								.originalFileName("chat_file_" + (k + 1) + ".jpg")
								.savedFileName("chat_file_" + (k + 1) + "_saved.jpg")
								.fileType("image")
								.fileUrl("http://dummyfile.com/chat_file_" + (k + 1) + "_saved.jpg")
								.build();
					}
					// 채팅의 발신자와 수신자가 반드시 다르도록 (이미 조건에 맞게 설정됨)
					chatRepository.save(chat);
				}
			}
		}

		// (4) 일반 User 계정 생성 (5명) – 이들은 TrainerApplication에 의해 Approved 처리된 클라이언트 계정으로 이미 생성됨

		// (5) Announcement 생성
		// (a) 관리자 작성 공지사항 (5건)
		for (int i = 1; i <= 5; i++) {
			AnnouncementEntity announcement = AnnouncementEntity.builder()
					.user(admin)
					.content("Admin Announcement " + i + ": New schedule updates.")
					.createdAt(LocalDateTime.now().minusHours(i))
					.build();
			announcementRepository.save(announcement);
		}
		// (b) 각 트레이너가 작성한 공지사항 (각 트레이너당 1건)
		List<TrainerEntity> trainers = trainerRepository.findAll();
		for (TrainerEntity trainer : trainers) {
			AnnouncementEntity trainerAnnouncement = AnnouncementEntity.builder()
					.user(trainer.getUser())
					.content("Trainer Announcement: Important update from " + trainer.getUser().getUserName())
					.createdAt(LocalDateTime.now().minusHours(random.nextInt(10) + 1))
					.build();
			announcementRepository.save(trainerAnnouncement);
		}

		// (6) Food 데이터: src/main/resources/food_insert.sql 의 각 쿼리를 읽어 실행
		try {
			List<String> foodSqlList = Files.readAllLines(Paths.get("src/main/resources/food_insert.sql"));
			for (String sql : foodSqlList) {
				if (!sql.trim().isEmpty()) {
					entityManager.createNativeQuery(sql).executeUpdate();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// (7) Meal 생성: 일반 User 중 TrainerApplication에서 Approved 상태인 계정만 사용
		Set<Long> approvedClientIds = trainerApplicationRepository.findAll()
				.stream()
				.filter(app -> app.getStatus() == Status.Approved)
				.map(app -> app.getUser().getUserId())
				.collect(Collectors.toSet());
		List<UserEntity> approvedClients = userRepository.findAllById(approvedClientIds);
		String[] mealTypes = { "Lunch", "Dinner", "Breakfast", "Snack", "Brunch" };
		for (int i = 1; i <= 5; i++) {
			// 승인된 일반 User 중에서 랜덤하게 선택
			if (!approvedClients.isEmpty()) {
				UserEntity mealUser = approvedClients.get(random.nextInt(approvedClients.size()));
				for (String mealType : mealTypes) {
					MealEntity meal = MealEntity.builder()
							.user(mealUser)
							.mealDate(LocalDate.now().minusDays(random.nextInt(10)))
							.totalCalories(600 + random.nextInt(200))
							.totalCarbs(80 + random.nextInt(20))
							.totalProtein(30 + random.nextInt(10))
							.totalFat(20 + random.nextInt(10))
							.originalFileName("meal_" + mealType + "_orig.jpg")
							.savedFileName("meal_" + mealType + "_saved.jpg")
							.mealType(mealType)
							.build();
					mealRepository.save(meal);
				}
			}
		}

		// (8) Comment 생성: 각 Work 및 Meal에 대해 0 ~ 5개의 댓글 무작위 생성
		List<WorkEntity> works = workRepository.findAll();
		for (WorkEntity work : works) {
			int numComments = random.nextInt(6); // 0~5개
			for (int i = 0; i < numComments; i++) {
				CommentEntity comment = CommentEntity.builder()
						.user(admin)
						.workout(work)
						.content("Work comment " + (i + 1) + " on workId " + work.getWorkoutId())
						.createdAt(LocalDateTime.now().minusMinutes(random.nextInt(60)))
						.build();
				commentRepository.save(comment);
			}
		}
		List<MealEntity> meals = mealRepository.findAll();
		for (MealEntity meal : meals) {
			int numComments = random.nextInt(6); // 0~5개
			for (int i = 0; i < numComments; i++) {
				CommentEntity comment = CommentEntity.builder()
						.user(admin)
						.meal(meal)
						.content("Meal comment " + (i + 1) + " on mealId " + meal.getMealId())
						.createdAt(LocalDateTime.now().minusMinutes(random.nextInt(60)))
						.build();
				commentRepository.save(comment);
			}
		}

		// (9) Chat 생성: Approved된 TrainerApplication에 해당하는 경우, 트레이너와 클라이언트 간의 채팅 생성
		for (TrainerEntity trainer : trainers) {
			List<TrainerApplicationEntity> approvedApps = trainerApplicationRepository
					.findByTrainerTrainerId(trainer.getTrainerId())
					.stream()
					.filter(app -> app.getStatus() == Status.Approved)
					.collect(Collectors.toList());
			if (!approvedApps.isEmpty()) {
				for (TrainerApplicationEntity app : approvedApps) {
					UserEntity client = app.getUser();
					int numMessages = random.nextInt(6); // 0~5개
					for (int k = 0; k < numMessages; k++) {
						// 발신자와 수신자는 번갈아 설정 (첫 메시지는 트레이너의 User, 다음은 클라이언트의 User)
						UserEntity sender = (k % 2 == 0) ? trainer.getUser() : client;
						UserEntity receiver = (k % 2 == 0) ? client : trainer.getUser();
						ChatEntity chat;
						boolean isTextMessage = random.nextBoolean();
						if (isTextMessage) {
							chat = ChatEntity.builder()
									.sender(sender)
									.receiver(receiver)
									.message("Chat message " + (k + 1) + " between Trainer userId "
											+ trainer.getUser().getUserId() + " and Client userId " + client.getUserId())
									.sentAt(LocalDateTime.now().minusMinutes(random.nextInt(60)))
									.isRead(random.nextBoolean())
									.originalFileName(null)
									.savedFileName(null)
									.fileType(null)
									.fileUrl(null)
									.build();
						} else {
							chat = ChatEntity.builder()
									.sender(sender)
									.receiver(receiver)
									.message(null)
									.sentAt(LocalDateTime.now().minusMinutes(random.nextInt(60)))
									.isRead(random.nextBoolean())
									.originalFileName("chat_file_" + (k + 1) + ".jpg")
									.savedFileName("chat_file_" + (k + 1) + "_saved.jpg")
									.fileType("image")
									.fileUrl("http://dummyfile.com/chat_file_" + (k + 1) + "_saved.jpg")
									.build();
						}
						// 채팅은 반드시 트레이너와 클라이언트가 서로 다르도록 생성
						if (!chat.getSender().getUserId().equals(chat.getReceiver().getUserId())) {
							chatRepository.save(chat);
						}
					}
				}
			}
		}

		System.out.println("모든 ddl 테이블에 대해 더미 데이터가 초기화되었습니다.");
	}
}