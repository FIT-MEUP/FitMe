package fitmeup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fitmeup.entity.TrainerEntity;

@Repository
public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {

}
