package fitmeup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import fitmeup.entity.TrainerApplicationEntity;

@Repository
public interface TrainerApplicationRepository extends JpaRepository<TrainerApplicationEntity, Long> {
    boolean existsByUserUserEmailAndTrainerTrainerId(String userEmail, Long trainerId);
}
