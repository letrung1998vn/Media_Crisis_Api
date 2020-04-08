package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.repositories.LastStandardRepository;

public class LastStandardService {
	@Autowired
	private LastStandardRepository lastStandardRepository;

	@Transactional
	public LastStandard getLastStandard(String keyword, String type, String numberType) {
		return lastStandardRepository.findByKeyWordAndTypeAndNumberType(keyword, type, numberType);
	}
	@Transactional
	public void save(LastStandard lastStandard) {
		lastStandardRepository.save(lastStandard);
	}
}
