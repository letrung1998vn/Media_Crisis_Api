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
import fpt.capstone.betatest.model.MessageOutputModel;
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
			List<Keyword> listKeyword = keywordService.getAll(username);
			Keyword keyword;
			for (int i = 0; i < listKeyword.size(); i++) {
				keyword = listKeyword.get(i);
				keyword.setAvailable(false);
				keywordService.saveKeyword(keyword);
			}
		} else {
			isAvailable = true;
			List<Keyword> listKeyword = keywordService.getAll(username);
			Keyword keyword;
			for (int i = 0; i < listKeyword.size(); i++) {
				keyword = listKeyword.get(i);
				keyword.setAvailable(true);
				keywordService.saveKeyword(keyword);
			}
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
	public MessageOutputModel updateProfile(@RequestParam(value = "userId") String userId, @RequestParam(value = "name") String name, 
			@RequestParam(value = "email") String email) {
		UserInfo info = userInfoService.getUserByUserId(userId);
		MessageOutputModel mod = new MessageOutputModel();
		if (!info.getUser().isAvailable()) {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		}
		else {
			info.setEmail(email);
			info.setName(name);
			info = userInfoService.saveUser(info);
			mod.setStatusCode(2);
			mod.setStatusMessage("Changed password successfully.");
		}
		
		return mod;
	}
	
	@PostMapping("updatePassword")
	public MessageOutputModel updatePassword(@RequestParam(value = "userName") String username, @RequestParam(value = "password") String password) {
		User user = userService.getUserByUserName(username);
		MessageOutputModel mod = new MessageOutputModel();
		if (!user.isAvailable()) {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		}
		else {
			user.setPassword(password);
			user = userService.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Changed password successfully.");
		}
		
		return mod;
	}
}