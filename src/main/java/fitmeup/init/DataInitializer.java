package fitmeup.init;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import fitmeup.entity.UserEntity;
import fitmeup.repository.UserRepository;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	

	@Override
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
                .build();
            userRepository.save(admin);
        }
	}

}
