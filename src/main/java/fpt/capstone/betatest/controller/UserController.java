package fpt.capstone.betatest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.CrisisModel;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.model.UserCrisis;
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
		return userService.changeUserStatus(username);
	}

	@PostMapping("login")
	public MessageOutputModel checkLogin(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password) {
		User result = userService.checkLogin(username, password);
		return userService.checkLogin(result);
	}

	@GetMapping("findAll")
	public List<User> findAll() {
		List<User> result = userService.findAll();
		return result;
	}

	@PostMapping("updateLinkWebhook")
	public MessageOutputModel updateWebhook(@RequestParam(value = "link") String link,
			@RequestParam(value = "username") String username) {
		return userService.updateWebhook(link, username);
	}

	@GetMapping("getUserKeyword")
	public MessageOutputModel findKeyword(@RequestParam(value = "username") String username) {
		User result = userService.getUserByUsername(username);
		return userService.findKeyword(result);
	}

	@GetMapping("getUserNoti")
	public MessageOutputModel findNoti(@RequestParam(value = "username") String username) {
		User result = userService.getUserByUsername(username);
		return userService.findNoti(result);
	}

	//
	@PostMapping("registration")
	public MessageOutputModel registration(@RequestParam(value = "username") String username,
			@RequestParam(value = "name") String name, @RequestParam(value = "password") String password,
			@RequestParam(value = "email") String email) {
		return userService.registration(username, password, name, email);
		
	}

	//
	@PostMapping("findAllUser")
	public Page<User> findUsers(@RequestParam(value = "username") String userId,
			@RequestParam(value = "page") int page) {
		Page<User> result = userService.searchByUsernameAndPage(userId, page);

		return result;
	}

	@PostMapping("updateProfile")
	public MessageOutputModel updateProfile(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "name") String name, @RequestParam(value = "email") String email) {
		User user = userService.getUserByUsername(userId);
		return userService.updateProfile(user, email, name);
	}

	@PostMapping("updatePassword")
	public MessageOutputModel updatePassword(@RequestParam(value = "userName") String username,
			@RequestParam(value = "password") String password) {
		User user = userService.getUserByUsername(username);
		return userService.updatePassword(user, password);
	}
	
	@PostMapping("disableWebhook")
	public MessageOutputModel disableWebhook(@RequestParam(value = "userName") String username) {
		User user = userService.getUserByUsername(username);
		return userService.disableWebhook(user);
	}
	
	@PostMapping("getAllUserCrisis")
	public List<CrisisModel> getAllUserCrisis(@RequestParam(value = "userName") String username) {
		User user = userService.getUserByUsername(username);
		return userService.getAllUserCrisis(user);
	}
}