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
import fpt.capstone.betatest.entities.NotificationToken;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.entities.UserInfo;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.model.UserLoginOutput;
import fpt.capstone.betatest.model.Webhook;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.NotificationTokenService;
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
	@Autowired
	NotificationTokenService notificationTokenService;
	@PostMapping("changeStatus")
	public MessageOutputModel changeUserStatus(@RequestParam(value = "username") String username) {
		User user = userService.getUserByUsername(username);
		MessageOutputModel mod = new MessageOutputModel();
		if (user != null) {
			boolean isAvailable = user.isAvailable();
			if (isAvailable) {
				isAvailable = false;
				List<Keyword> listKeyword = keywordService.getAll(user);
				Keyword keyword;
				for (int i = 0; i < listKeyword.size(); i++) {
					keyword = listKeyword.get(i);
					keyword.setAvailable(false);
					keywordService.saveKeyword(keyword);
				}
			} else {
				isAvailable = true;
				List<Keyword> listKeyword = keywordService.getAll(user);
				Keyword keyword;
				for (int i = 0; i < listKeyword.size(); i++) {
					keyword = listKeyword.get(i);
					keyword.setAvailable(true);
					keywordService.saveKeyword(keyword);
				}
			}
			user.setAvailable(isAvailable);
			if (userService.saveUser(user) != null) {
				mod.setStatusCode(2);
				mod.setStatusMessage("User status changed!");
			} else {
				mod.setStatusCode(4);
				mod.setStatusMessage("User status changed fail, please try again!");
			}
		} else {
			mod.setStatusCode(4);
			mod.setStatusMessage("This user is not exist anymore!");
		}
		return mod;
	}

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

				mod.setStatusMessage(
						"Your account has been banned permanently, please contact admin for more information!");
			}
		}
		return mod;
	}

	@GetMapping("findAll")
	public List<User> findAll() {
		List<User> result = userService.findAll();
		return result;
	}

	@PostMapping("updateLinkWebhook")
	public MessageOutputModel updateWebhook(@RequestParam(value = "link") String link,
			@RequestParam(value = "username") String username) {
		MessageOutputModel mod = new MessageOutputModel();
		User user = userService.getUserByUsername(username);
		if (user.isAvailable()) {
			user.getUser().setLink_webhook(link);
			userService.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Webhook link is saved!");
			mod.setObj(user.getUser().getLink_webhook());
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more infomation!");
		}
		return mod;
	}

	@GetMapping("getUserKeyword")
	public MessageOutputModel findKeyword(@RequestParam(value = "username") String username) {
		System.out.println(username);
		User result = userService.getUserByUsername(username);
		System.out.println(result.toString());
		MessageOutputModel mod = new MessageOutputModel();
		if (result.isAvailable()) {
			if (!result.getKeyword().isEmpty()) {
				mod.setObj(result.getKeyword());
				mod.setStatusCode(2);
				mod.setStatusMessage("Get keyword success.");
			} else {
				mod.setStatusCode(4);
				mod.setStatusMessage("Your keyword list is empty.");
			}

		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more infomation!");
		}
		return mod;
	}

	@GetMapping("getUserNoti")
	public MessageOutputModel findNoti(@RequestParam(value = "username") String username) {
		User result = userService.getUserByUsername(username);
		System.out.println(result.toString());
		MessageOutputModel mod = new MessageOutputModel();
		if (result.isAvailable()) {
			mod.setStatusCode(2);
			mod.setStatusMessage("Get noti success");
			mod.setObj(result.getNotifications());
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage(
					"Your account has been banned permanently, please contact admin for more information!");
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
				userInfo.setLink_webhook("");
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
	@PostMapping("findAllUser")
	public Page<User> findUsers(@RequestParam(value = "username") String userId,
			@RequestParam(value = "page") int page) {
		Page<User> result = userService.searchByUsernameAndPage(userId, page);

		return result;
	}
	//
	// @GetMapping("findAllUser")
	// public List<User> findAllUser() {
	// List<User> result = userService.getAll();
	// return result;
	// }

	@PostMapping("updateProfile")
	public MessageOutputModel updateProfile(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "name") String name, @RequestParam(value = "email") String email) {
		User user = userService.getUserByUsername(userId);
		MessageOutputModel mod = new MessageOutputModel();
		if (!user.isAvailable()) {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		} else {
			user.getUser().setEmail(email);
			user.getUser().setName(name);
			user = userService.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Changed userprofile successfully.");
		}

		return mod;
	}

	@PostMapping("updatePassword")
	public MessageOutputModel updatePassword(@RequestParam(value = "userName") String username,
			@RequestParam(value = "password") String password) {
		User user = userService.getUserByUsername(username);
		MessageOutputModel mod = new MessageOutputModel();
		if (!user.isAvailable()) {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		} else {
			user.setPassword(password);
			user = userService.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Changed password successfully.");
		}
		return mod;
	}
	
	@PostMapping("disableWebhook")
	public MessageOutputModel disableWebhook(@RequestParam(value = "userName") String username) {
		User user = userService.getUserByUsername(username);
		MessageOutputModel mod = new MessageOutputModel();
		if (!user.isAvailable()) {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		} else {
			user.getUser().setLink_webhook("");
			user = userService.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Disable webhook notification successfully.");
		}
		return mod;
	}
}