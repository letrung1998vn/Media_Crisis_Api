package fpt.capstone.betatest.services;


import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.entities.UserInfo;
import fpt.capstone.betatest.repositories.UserInfoRepository;
import fpt.capstone.betatest.repositories.UserRepository;

@Service
public class UserInfoService {
	@Autowired
	private UserInfoRepository usersInfoRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Transactional
	public UserInfo saveUser(UserInfo u) {
		return usersInfoRepository.save(u);
	}
	
	@Transactional
	public String getEmail(String userId) {
		return usersInfoRepository.findByUserId(userId).getEmail();
	}

//	@Transactional
//	public Page<UserInfo> searchByUsernameAndPage(String userId, int Page) {
//		Pageable page = PageRequest.of((Page - 1), 10);
//		return usersInfoRepository.findByUserIdContaining(userId, page);
//	}
//	
//	@Transactional
//	public UserInfo getByUser(User user) {
//		return usersInfoRepository.findByUser(user);
//	}
//	
//	@Transactional
//	public UserInfo getByUsernameAndPassword(String username, String password) {
//		return usersInfoRepository.findByUser(userRepository.findByUserNameAndPassword(username, password));
//	}
//	
//	@Transactional
//	public UserInfo getUserByUserId(String userId) {
//		return usersInfoRepository.findByUserId(userId);
//	}
}
