package fpt.capstone.betatest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.UserEntity;
import fpt.capstone.betatest.repositories.UserRepository;;

@Service
public class UserService {
	@Autowired
	private UserRepository usersRepository;
	
	@Transactional
	public UserEntity checkLogin(String username, String password) {
		return usersRepository.findByUsernameAndPassword(username, password);
	}
	
	@Transactional
	public List<UserEntity> getAll() {
		return usersRepository.findAll();
	}
}
