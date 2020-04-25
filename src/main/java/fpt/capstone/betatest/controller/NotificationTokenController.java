package fpt.capstone.betatest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.services.NotificationTokenService;
import fpt.capstone.betatest.services.UserService;

@RestController
@RequestMapping("/notificationToken")
public class NotificationTokenController {
	@Autowired
	UserService userService;
	@Autowired
	NotificationTokenService notificationTokenService;

	@PostMapping("enableNotiToken")
	public MessageOutputModel updateNotiToken(@RequestParam(value = "token") String token,
			@RequestParam(value = "username") String username) {
		User user = userService.getUserByUsername(username);
		return notificationTokenService.updateNotiToken(user, username, token);
	}

	@PostMapping("checkNotiTokenExist")
	public MessageOutputModel checkExist(@RequestParam(value = "token") String token,
			@RequestParam(value = "username") String username) {
		return notificationTokenService.checkExist(username, token);
	}

	@PostMapping("disableNotiToken")
	public MessageOutputModel disableNotiToken(@RequestParam(value = "token") String token,
			@RequestParam(value = "username") String username) {
		
		User user = userService.getUserByUsername(username);
		return notificationTokenService.disableNotiToken(user, username, token);
	}
}
