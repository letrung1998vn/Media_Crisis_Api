package fpt.capstone.betatest.services;

import java.math.BigInteger;

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
	public Crisis findCrisis(BigInteger ContentId, String Type) {
		return crisisRepository.findByContentIdAndType(ContentId, Type);
	}
}
