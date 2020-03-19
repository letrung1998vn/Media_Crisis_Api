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

//	@PostMapping("changeStatus")
//	public UserInfo changeUserStatus(@RequestParam(value = "username") String username) {
//		UserInfo user = userInfoService.getByUser(userService.getByUsername(username));
//		boolean isAvailable = user.getUser().isAvailable();
//		if (isAvailable) {
//			isAvailable = false;
//			List<Keyword> listKeyword = keywordService.getAll(username);
//			Keyword keyword;
//			for (int i = 0; i < listKeyword.size(); i++) {
//				keyword = listKeyword.get(i);
//				keyword.setAvailable(false);
//				keywordService.saveKeyword(keyword);
//			}
//		} else {
//			isAvailable = true;
//			List<Keyword> listKeyword = keywordService.getAll(username);
//			Keyword keyword;
//			for (int i = 0; i < listKeyword.size(); i++) {
//				keyword = listKeyword.get(i);
//				keyword.setAvailable(true);
//				keywordService.saveKeyword(keyword);
//			}
//		}
//		user.getUser().setAvailable(isAvailable);
//		return userInfoService.saveUser(user);
//	}

	@PostMapping("login")
	public MessageOutputModel checkLogin(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password) {
		MessageOutputModel mod = new MessageOutputModel();
		User result = userService.checkLogin(username, password);
		if (result == null) {
			mod.setStatusCode(4);
			mod.setStatusMessage("Invalid username or password, please try again!");
		} else {
			if (result.isAvailable()) {
				mod.setStatusCode(2);
				mod.setStatusMessage("Welcome");
				result.setKeyword(null);
				mod.setObj(result);
			} else {
				mod.setStatusCode(3);
				mod.setStatusMessage("Your account has been banned permanently, please contact admin for more information!");
			}
		}
		return mod;
	}

	@GetMapping("findAll")
	public List<User> findAll() {
		List<User> result = userService.findAll();
		return result;
	}
	
	@GetMapping("getUserKeyword")
	public MessageOutputModel findKeyword(@RequestParam(value = "username") String username) {
		User result = userService.getUserByUsername(username);
		MessageOutputModel mod = new MessageOutputModel();
		if (result.isAvailable()) {
			mod.setStatusCode(2);
			mod.setStatusMessage("Get keyword success");
			mod.setObj(result.getKeyword());
		
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more information!");
		}
		return mod;
	}
//
	@PostMapping("registration")
	public MessageOutputModel registration(@RequestParam(value = "username") String username,
			@RequestParam(value = "name") String name, @RequestParam(value = "password") String password,
			@RequestParam(value = "email") String email) {
		MessageOutputModel mod = new MessageOutputModel();
		User user = new User();
		UserInfo userInfo = new UserInfo();
		if (userService.checkUserExist(username)) {
			try {
				user.setUserName(username);
				user.setPassword(password);
				user.setRole("user");
				user.setAvailable(true);
				user = userService.saveUser(user);
				userInfo.setUserId(username);
				userInfo.setName(name);
				userInfo.setEmail(email);
				userInfo = userInfoService.saveUser(userInfo);
				mod.setStatusCode(2);
				mod.setStatusMessage("Sign up successfully, please login!");
			} catch (Exception e) {
				mod.setStatusCode(4);
				mod.setStatusMessage(e.getMessage());
			}
		} else {
			mod.setStatusCode(4);
			mod.setStatusMessage("This username is existed, please pick another!");
		}
		return mod;
	}
//
//	@GetMapping("findAllUserInfo")
//	public Page<UserInfo> findUserInfo(@RequestParam(value = "username") String userId, @RequestParam(value = "page") int page) {
//		Page<UserInfo> result = userInfoService.searchByUsernameAndPage(userId, page);
//		return result;
//	}
//
//	@GetMapping("findAllUser")
//	public List<User> findAllUser() {
//		List<User> result = userService.getAll();
//		return result;
//	}
//	
//	@PostMapping("updateProfile")
//	public MessageOutputModel updateProfile(@RequestParam(value = "userId") String userId, @RequestParam(value = "name") String name, 
//			@RequestParam(value = "email") String email) {
//		UserInfo info = userInfoService.getUserByUserId(userId);
//		MessageOutputModel mod = new MessageOutputModel();
//		if (!info.getUser().isAvailable()) {
//			mod.setStatusCode(3);
//			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
//		}
//		else {
//			info.setEmail(email);
//			info.setName(name);
//			info = userInfoService.saveUser(info);
//			mod.setStatusCode(2);
//			mod.setStatusMessage("Changed password successfully.");
//		}
//		
//		return mod;
//	}
//	
//	@PostMapping("updatePassword")
//	public MessageOutputModel updatePassword(@RequestParam(value = "userName") String username, @RequestParam(value = "password") String password) {
//		User user = userService.getUserByUserName(username);
//		MessageOutputModel mod = new MessageOutputModel();
//		if (!user.isAvailable()) {
//			mod.setStatusCode(3);
//			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
//		}
//		else {
//			user.setPassword(password);
//			user = userService.saveUser(user);
//			mod.setStatusCode(2);
//			mod.setStatusMessage("Changed password successfully.");
//		}
//		
//		return mod;
//	}
}