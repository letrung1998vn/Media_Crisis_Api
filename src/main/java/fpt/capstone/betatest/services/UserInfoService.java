package fpt.capstone.betatest.services;


import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.UserInfo;
import fpt.capstone.betatest.repositories.UserInfoRepository;

@Service
public class UserInfoService {
	@Autowired
	private UserInfoRepository usersInfoRepository;
	
	@Transactional
	public UserInfo saveUser(UserInfo u) {
		return usersInfoRepository.save(u);
	}
	
	@Transactional
	public String getEmail(String userId) {
		return usersInfoRepository.findByUserId(userId).getEmail();
	}

}
