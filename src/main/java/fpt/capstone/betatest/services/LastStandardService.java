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
	
	
	
	@Transactional
	public double calUpperLimit(double standard, double mean) {
		double anomaly_cut_off = standard * 2;
		double upper_limit = mean + anomaly_cut_off;
		return upper_limit;
	}
	
}
