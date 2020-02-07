package fpt.capstone.betatest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.services.UserService;

@RestController
public class LoginController {
	@Autowired
	UserService userService;
	
	@GetMapping("login")
    public User checkLogin(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {
		User result = userService.checkLogin(username, password);
        return result;
    }
	
	@GetMapping("check")
    public User checkExist(@RequestParam(value = "username") String username) {
		User result = userService.getByUsername(username);
        return result;
    }
	
	@GetMapping("findAll")
    public List<User> findAllUser() {
		List<User> result = userService.getAll();
        return result;
    }
	
}