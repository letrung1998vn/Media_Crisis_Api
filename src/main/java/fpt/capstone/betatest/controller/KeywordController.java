package fpt.capstone.betatest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.UserService;

@RestController
@RequestMapping("/keyword")
public class KeywordController {
	@Autowired
	KeywordService keywordService;
	@Autowired
	private UserService userService;

	@GetMapping("findAll")
	public MessageOutputModel findAll() {
		return keywordService.findAll();
	}

	@GetMapping("getUsers")
	public List<String> getUsers() {
		return keywordService.getUsers();
	}

	@PostMapping("search")
	public Page<Keyword> getKeyword(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "username") String username, @RequestParam(value = "page") int page) {
		return keywordService.getKeyword(username, keyword, page);
	}

	@PostMapping("createKeyword")
	public MessageOutputModel createKeyword(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "userId") String userId, @RequestParam(value = "crisis_rate") String crisisRateString) {
		Keyword kw = new Keyword();
		User user = userService.getUserByUsername(userId);
		double crisisRate = Double.parseDouble(crisisRateString);
		System.out.println(crisisRate);
		return keywordService.createKeyword(user, kw, keyword, crisisRate);
	}

	@PostMapping("updateKeyword")
	public MessageOutputModel updateKeyword(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "keywordId") int keywordId, @RequestParam(value = "logVersion") int log_version,
			@RequestParam(value = "author") String author, @RequestParam(value = "crisis_rate") String crisisRateString) {
		Keyword kw = keywordService.getKeywordById(keywordId);
		User user = userService.getUserByUsername(author);
		double crisisRate = Double.parseDouble(crisisRateString);
		System.out.println(crisisRate);
		return keywordService.updateKeyword(user, kw, keyword, log_version, keywordId, crisisRate);
	}

	@PostMapping("deleteKeyword")
	public MessageOutputModel deleteKeyword(@RequestParam(value = "id") int id,
			@RequestParam(value = "logVersion") int log_version, @RequestParam(value = "author") String author) {	
		User user = userService.getUserByUsername(author);
		return keywordService.deleteKeyword(user, log_version, id);
	}

}
