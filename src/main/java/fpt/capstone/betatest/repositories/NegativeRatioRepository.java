package fpt.capstone.betatest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.NegativeRatio;

@Repository("negativeRatioRepository")
public interface NegativeRatioRepository extends JpaRepository<NegativeRatio, Integer> {
	NegativeRatio findByKeywordAndType(String keyword, String type);
}
