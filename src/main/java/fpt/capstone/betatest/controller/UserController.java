package fpt.capstone.betatest.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.entities.UserInfo;
import fpt.capstone.betatest.model.UserLoginOutput;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.UserInfoService;
import fpt.capstone.betatest.services.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	UserService userService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	KeywordService keywordService;

	@PostMapping("changeStatus")
	public UserInfo changeUserStatus(@RequestParam(value = "username") String username) {
		UserInfo user = userInfoService.getByUser(userService.getByUsername(username));
		boolean isAvailable = user.getUser().isAvailable();
		if (isAvailable) {
			isAvailable = false;
		} else {
			isAvailable = true;
		}
		user.getUser().setAvailable(isAvailable);
		return userInfoService.saveUser(user);
	}

	@GetMapping("login")
	public UserInfo checkLogin(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password) {
		UserInfo result = userInfoService.getByUsernameAndPassword(username, password);
		return result;
	}

//	@GetMapping("check")
//	public List<UserInfo> checkExist() {
//		List<UserInfo> result = userInfoService.getAll();
//		return result;
//	}

	@PostMapping("registration")
	public UserInfo registration(@RequestParam(value = "username") String username,
			@RequestParam(value = "name") String name, @RequestParam(value = "password") String password,
			@RequestParam(value = "email") String email) {
		UserInfo userInfo = new UserInfo();
		User user = new User();
		if (userService.getByUsername(username) == null) {

			user.setUserName(username);
			user.setPassword(password);
			user.setRole("user");
			user.setAvailable(true);
			user = userService.saveUser(user);

			userInfo.setUserId(username);
			userInfo.setName(name);
			userInfo.setEmail(email);
			userInfo = userInfoService.saveUser(userInfo);
		} else {
			userInfo.setUserId("");
			userInfo.setName("");
			userInfo.setEmail("");
		}
		return userInfo;
	}

	@GetMapping("findAllUserInfo")
	public Page<UserInfo> findUserInfo(@RequestParam(value = "username") String userId, @RequestParam(value = "page") int page) {
		Page<UserInfo> result = userInfoService.searchByUsernameAndPage(userId, page);
		return result;
	}

	@GetMapping("findAllUser")
	public List<User> findAllUser() {
		List<User> result = userService.getAll();
		return result;
	}
	
	@PostMapping("updateProfile")
	public UserInfo updateProfile(@RequestParam(value = "userId") String userId, @RequestParam(value = "name") String name, 
			@RequestParam(value = "email") String email) {
		UserInfo info = userInfoService.getUserByUserId(userId);
		info.setEmail(email);
		info.setName(name);
		info = userInfoService.saveUser(info);
		return info;
	}

}