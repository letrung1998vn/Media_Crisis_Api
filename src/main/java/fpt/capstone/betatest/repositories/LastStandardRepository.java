package fpt.capstone.betatest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.LastStandard;
@Repository("lastStandardRepository")
public interface LastStandardRepository extends JpaRepository<LastStandard, Integer>{
	LastStandard findByKeywordAndTypeAndNumberType(String keyword, String type, String numberType);
}
