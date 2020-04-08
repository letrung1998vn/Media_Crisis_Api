package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fpt.capstone.betatest.entities.LastStandard;

public interface LastStandardRepository extends JpaRepository<LastStandard, Integer>{
	LastStandard findByKeyWordAndTypeAndNumberType(String keyword, String type, String numberType);
}
