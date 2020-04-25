
package fpt.capstone.betatest.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aylien.textapi.TextAPIClient;

import fpt.capstone.betatest.services.CheckService;



@RestController
@RequestMapping("/checkMeaning")

public class CheckMeaningController {
	
	@GetMapping("check")
	public void checkMeaning() throws Exception {
		TextAPIClient client = new TextAPIClient("43faa103", "f2aaee05b21dabe934b89bd3198801e8");
		CheckService check = new CheckService();
		check.setData(client);
		check.start();
	}

}

