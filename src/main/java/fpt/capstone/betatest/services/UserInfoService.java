package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
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
	public List<UserInfo> getAll() {
		return usersInfoRepository.findAll();
	}
	
	@Transactional
	public UserInfo getByUser(User user) {
		return usersInfoRepository.findByUser(user);
	}
	
	@Transactional
	public UserInfo getByUsernameAndPassword(String username, String password) {
		return usersInfoRepository.findByUser(userRepository.findByUserNameAndPassword(username, password));
	}
}
