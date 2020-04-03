package fpt.capstone.betatest.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.NegativeRatio;
import fpt.capstone.betatest.repositories.NegativeRatioRepository;
@Service
public class NegativeRatioService {
	@Autowired
	private NegativeRatioRepository negativeRatioRepository;
	@Transactional
	public NegativeRatio getNegativeRatio(String keyword, String type) {
		return negativeRatioRepository.findByKeywordAndType(keyword, type);
	}
	@Transactional
	public void save(NegativeRatio negativeRatio) {
		negativeRatioRepository.save(negativeRatio);
	}
}
