package fpt.capstone.betatest.services;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.repositories.CrisisRepository;

@Service
public class CrisisService {
	@Autowired
	private CrisisRepository crisisRepository;

	@Transactional
	public void saveCrisis(Crisis crisis) {
		crisisRepository.save(crisis);
	}

	@Transactional
	public boolean checkCrisisExist(BigInteger ContentId, String Type) {
		boolean result = false;
		Crisis crisis = crisisRepository.findByContentIdAndType(ContentId, Type);
		if (crisis == null) {
			result = true;
		}
		return result;
	}
	
	@Transactional
	public List<Crisis> getCrisisByKeyword(String keyword) {
		return crisisRepository.findByKeyword(keyword);
	}
}
