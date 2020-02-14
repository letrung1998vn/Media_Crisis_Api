package fpt.capstone.betatest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.entities.UserInfo;
import fpt.capstone.betatest.services.UserInfoService;
import fpt.capstone.betatest.services.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	UserService userService;
	@Autowired
	UserInfoService userInfoService;
	
	@GetMapping("login")
    public UserInfo checkLogin(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {
		UserInfo result = userInfoService.getByUsernameAndPassword(username, password);
        return result;
    }
	
	@GetMapping("check")
    public List<UserInfo> checkExist() {
		List<UserInfo> result = userInfoService.getAll();
        return result;
    }
	
	@PostMapping("registration")
	public UserInfo registration(@RequestParam(value = "username") String username, @RequestParam(value = "name") String name, @RequestParam(value = "password") String password, @RequestParam(value = "email") String email) {
		
		User user = new User();
		user.setUserName(username);
		user.setPassword(password);
		user.setRole("user");
		user = userService.saveUser(user);
		
		UserInfo userInfo = new UserInfo();
		userInfo.setUserId(username);
		userInfo.setName(name);
		userInfo.setEmail(email);
		userInfo = userInfoService.saveUser(userInfo);
		
		
		return userInfo;
    }
	
	@GetMapping("findAllUserInfo")
    public List<UserInfo> findAllUserInfo() {
		List<UserInfo> result = userInfoService.getAll();
        return result;
    }
	
	@GetMapping("findAllUser")
    public List<User> findAllUser() {
		List<User> result = userService.getAll();
        return result;
    }
	
}