package fpt.capstone.betatest.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.repositories.LastStandardRepository;
@Service
public class LastStandardService {
	@Autowired
	private LastStandardRepository lastStandardRepository;

	@Transactional
	public LastStandard getLastStandard(String keyword, String type, String numberType) {
		return lastStandardRepository.findByKeywordAndTypeAndNumberType(keyword, type, numberType);
	}
	@Transactional
	public void save(LastStandard lastStandard) {
		lastStandardRepository.save(lastStandard);
	}
}
