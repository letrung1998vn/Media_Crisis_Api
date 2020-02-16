package fpt.capstone.betatest.controller;

import java.util.ArrayList;
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
	
	@GetMapping("login")
    public List<UserLoginOutput> checkLogin(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {
		List<UserLoginOutput> output = new ArrayList<UserLoginOutput>();
		UserInfo result = userInfoService.getByUsernameAndPassword(username, password);
		List<Keyword> resultKeyword = keywordService.getAll(result.getUserId());
		if (resultKeyword.size() == 0) {
			UserLoginOutput infoOutPut1 = new UserLoginOutput(result.getUserId(), result.getUser().getPassword(), result.getEmail(), result.getName(), "", 0, result.getUser().getRole());
			System.out.println(infoOutPut1);
			output.add(infoOutPut1);
		}else {
			UserLoginOutput infoOutPut2 = new UserLoginOutput();
			for (int i = 0; i < resultKeyword.size(); i++) {
				infoOutPut2.setName(result.getName());
				infoOutPut2.setEmail(result.getEmail());
				infoOutPut2.setUserId(result.getUserId());				
				infoOutPut2.setPassword(result.getUser().getPassword());			
				infoOutPut2.setRole(result.getUser().getRole());			
				infoOutPut2.setKeywordId(resultKeyword.get(i).getId());
				infoOutPut2.setKeyword(resultKeyword.get(i).getKeyword());
				System.out.println(infoOutPut2.toString());
				output.add(infoOutPut2);
				infoOutPut2 = new UserLoginOutput();
			}
		}
        return output;
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