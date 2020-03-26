package fpt.capstone.betatest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.NotificationToken;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.NotificationTokenService;
import fpt.capstone.betatest.services.UserInfoService;
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
		MessageOutputModel mod = new MessageOutputModel();
		User user = userService.getUserByUsername(username);
		if (user.isAvailable()) {
			NotificationToken notiToken = new NotificationToken();
			notiToken.setNotiToken(token);
			notiToken.setUserName(username);
			notificationTokenService.saveToken(notiToken);
			mod.setStatusCode(2);
			mod.setStatusMessage("Regist notification for browser success!");
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more infomation!");
		}
		return mod;
	}

	@PostMapping("checkNotiTokenExist")
	public MessageOutputModel checkExist(@RequestParam(value = "token") String token,
			@RequestParam(value = "username") String username) {
		boolean result = false;
		MessageOutputModel mod = new MessageOutputModel();
		mod.setStatusCode(0);
		NotificationToken notiToken = notificationTokenService.getNotiTokenByUserIdAndNotiToken(username, token);
		if (notiToken != null) {
			mod.setStatusCode(1);
		}
		return mod;
	}

	@PostMapping("disableNotiToken")
	public MessageOutputModel disableNotiToken(@RequestParam(value = "token") String token,
			@RequestParam(value = "username") String username) {
		MessageOutputModel mod = new MessageOutputModel();
		User user = userService.getUserByUsername(username);
		if (user.isAvailable()) {
			NotificationToken notiToken = notificationTokenService.getNotiTokenByUserIdAndNotiToken(username, token);
			notificationTokenService.deleteToken(notiToken);
			mod.setStatusCode(2);
			mod.setStatusMessage("Disable notification for browser success!");
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more infomation!");
		}
		return mod;
	}
}
