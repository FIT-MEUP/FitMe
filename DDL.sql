--  FIT_ME_db
CREATE DATABASE IF NOT EXISTS FIT_ME_db;
USE FIT_ME_db;

-- 1. 사용자 (User)
CREATE TABLE user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    password_hash VARCHAR(255) NULL,
    user_name VARCHAR(100) NOT NULL,
    user_gender ENUM('Male', 'Female', 'Other') NOT NULL,
    user_birthdate DATE NOT NULL,
    user_email VARCHAR(255) UNIQUE NOT NULL,
    user_contact VARCHAR(20) UNIQUE NOT NULL,
    role ENUM('User', 'Trainer', 'Admin') NOT NULL DEFAULT 'User'
);

-- 2. PT 세션 내역 (PTSessionHistory)
CREATE TABLE pt_session_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    change_type ENUM('Added', 'Deducted') NOT NULL,
    change_amount INT NOT NULL,
    change_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    reason TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 3. 트레이너 (Trainer)
CREATE TABLE trainer (
    trainer_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    experience INT NOT NULL,
    fee DECIMAL(10,2) NOT NULL,
    bio TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 4. 트레이너 신청 (TrainerApplication)
CREATE TABLE trainer_application (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    trainer_id INT NOT NULL,
    status ENUM('Pending', 'Approved', 'Rejected') NOT NULL,
    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    response_at DATETIME NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainer(trainer_id) ON DELETE CASCADE
);

-- 5. 트레이너 사진 (TrainerPhoto)
CREATE TABLE trainer_photo (
    photo_id INT PRIMARY KEY AUTO_INCREMENT,
    trainer_id INT NOT NULL,
    photo_url VARCHAR(1000) NOT NULL,
    FOREIGN KEY (trainer_id) REFERENCES trainer(trainer_id) ON DELETE CASCADE
);

-- 6. 일정 (Schedule)
CREATE TABLE schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    trainer_id INT NOT NULL,
    user_id INT NOT NULL,
    status ENUM('Pending', 'Approved', 'Rejected') NOT NULL,
    attendance_status ENUM('Present', 'Absent', 'PT Session') NOT NULL,
    session_deducted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (trainer_id) REFERENCES trainer(trainer_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 7. 건강 데이터 (HealthData)
CREATE TABLE health_data (
    data_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    muscle_mass DECIMAL(5,2) NOT NULL,
    height DECIMAL(5,2) NOT NULL,
    bmi DECIMAL(5,2) NOT NULL,
    basal_metabolic_rate DECIMAL(5,2) NOT NULL,
    record_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 8. 운동 (Workout)
CREATE TABLE workout (
    workout_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    part VARCHAR(255) NOT NULL,
    exercise VARCHAR(255) NOT NULL,
    sets INT NOT NULL,
    reps INT NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    schedule_id INT NULL,
    workout_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedule(schedule_id) ON DELETE SET NULL
);

-- 9. 운동 데이터 (WorkoutData)
CREATE TABLE workout_data (
    data_id INT PRIMARY KEY AUTO_INCREMENT,
    workout_id INT NOT NULL,
    original_file_name VARCHAR(500) NULL,
    saved_file_name VARCHAR(500) NULL,
    FOREIGN KEY (workout_id) REFERENCES workout(workout_id) ON DELETE CASCADE
);

-- 10. 채팅 (Chat)
CREATE TABLE chat (
    chat_id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message TEXT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    original_file_name VARCHAR(500) NULL,
    saved_file_name VARCHAR(500) NULL,
    file_type ENUM('image', 'video', 'document', 'audio') NULL,
    file_url VARCHAR(1000) NULL,
    FOREIGN KEY (sender_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 11. 공지사항 (Announcement)
CREATE TABLE announcement (
    announcement_id INT PRIMARY KEY AUTO_INCREMENT,
    author_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 12. 알림 (Notification)
CREATE TABLE notification (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    type ENUM('Session Approval', 'Reminder', 'Feedback', 'Announcement') NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 13. 식단 (Meal)
CREATE TABLE meal (
    meal_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    meal_date DATETIME NOT NULL,
    total_calories DECIMAL(6,2) NOT NULL,
    total_carbs DECIMAL(6,2) NOT NULL,
    total_protein DECIMAL(6,2) NOT NULL,
    total_fat DECIMAL(6,2) NOT NULL,
    original_file_name VARCHAR(500) NULL,
    saved_file_name VARCHAR(500) NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 14. 음식 (Food)
CREATE TABLE food (
    food_id INT PRIMARY KEY AUTO_INCREMENT,
    meal_id INT NOT NULL,
    food_name VARCHAR(255) NOT NULL,
    calories DECIMAL(6,2) NOT NULL,
    carbs DECIMAL(6,2) NOT NULL,
    protein DECIMAL(6,2) NOT NULL,
    fat DECIMAL(6,2) NOT NULL,
    FOREIGN KEY (meal_id) REFERENCES meal(meal_id) ON DELETE CASCADE
);

-- 15. 댓글 (Comment)
CREATE TABLE comment (
    comment_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    workout_id INT NULL,
    meal_id INT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (workout_id) REFERENCES workout(workout_id) ON DELETE CASCADE,
    FOREIGN KEY (meal_id) REFERENCES meal(meal_id) ON DELETE CASCADE
);

ALTER TABLE user ADD COLUMN is_online BOOLEAN DEFAULT FALSE;
ALTER TABLE user CHANGE COLUMN password_hash password VARCHAR(255) NOT NULL;

