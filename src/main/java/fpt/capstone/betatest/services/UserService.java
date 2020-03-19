package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.repositories.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository usersRepository;
	
	@Transactional
	public User checkLogin(String username, String password) {
		return usersRepository.findByUserNameAndPassword(username, password);
	}
	
	@Transactional
	public List<User> findAll() {
		return usersRepository.findAll();
	}
//	
	@Transactional
	public boolean checkUserExist(String username) {
		boolean result = false;
		User user = usersRepository.findByUserName(username);
		if (user == null) {
			result = true;
		}
		return result;
	}
	
	@Transactional
	public User getUserByUsername(String username) {
		User user = usersRepository.findByUserName(username);
		return user;
	}
	
	@Transactional
	public User saveUser(User u) {
		return usersRepository.save(u);
	}
	
	@Transactional
	public Page<User> searchByUsernameAndPage(String userId, int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return usersRepository.findByUserNameContaining(userId, page);
	}
	
	
//	@Transactional
//	public User getUserByUserName(String userName) {
//		return usersRepository.findByUserName(userName);
//	}
}
