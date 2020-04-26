
package fpt.capstone.betatest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aylien.textapi.TextAPIClient;

import fpt.capstone.betatest.services.CheckService;
import fpt.capstone.betatest.services.UserService;



@RestController
@RequestMapping("/checkMeaning")

public class CheckMeaningController {
	@Autowired
	CheckService check;
	
	@GetMapping("check")
	public void checkMeaning() throws Exception {
		TextAPIClient client = new TextAPIClient("e6d02d73", "5b2cf266e4f8e1f86f413065e03dafc1");
		check.setData(client);
		check.start();
	}

}

