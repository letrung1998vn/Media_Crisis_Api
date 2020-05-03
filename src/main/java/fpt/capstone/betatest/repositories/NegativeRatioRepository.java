package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.NegativeRatio;

@Repository("negativeRatioRepository")
public interface NegativeRatioRepository extends JpaRepository<NegativeRatio, Integer> {
	List<NegativeRatio> findByKeywordAndTypeOrderByUpdateDateDesc(String keyword, String type);
}
