package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
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
	public List<User> getAll() {
		return usersRepository.findAll();
	}
	
	@Transactional
	public User getByUsername(String username) {
		return usersRepository.checkUserexist(username);
	}
	
	@Transactional
	public User saveUser(User u) {
		return usersRepository.save(u);
	}
}
