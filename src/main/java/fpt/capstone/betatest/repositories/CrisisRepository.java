package fpt.capstone.betatest.repositories;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Crisis;

@Repository("crisisRepository")
public interface CrisisRepository extends JpaRepository<Crisis, Integer> {
	Crisis findByContentIdAndTypeAndKeywordAndDetectType(BigInteger ContentId, String Type, String keyword, String detectType);

	List<Crisis> findByKeyword(String keyword);
	
	Crisis findById(int id);
}
