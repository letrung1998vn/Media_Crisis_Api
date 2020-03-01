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
import fpt.capstone.betatest.repositories.KeywordRepository;
import fpt.capstone.betatest.services.KeywordService;

@RestController
@RequestMapping("/keyword")
public class KeywordController {
	@Autowired
	KeywordService keywordService;
	@Autowired
	private KeywordRepository keywordsRepository;
	
	@GetMapping("getUsers")
	public List<String> getUsers() {
		return keywordService.getAllUserHaveKeyword();
	}
	
	@PostMapping("search")
	public Page<Keyword> getKeyword(@RequestParam(value = "keyword") String keyword, @RequestParam(value = "username") String username, @RequestParam(value = "page") int page) {
		Page<Keyword> result = null;
		if (username.equals("")) {
			result = keywordService.searchKeyword(keyword, page);
		} else {
			result = keywordService.searchKeywordByUserIdAndKeywordContain(keyword, username, page);	
		}
		
		return result;
	}
	
	@GetMapping("check")
	public Keyword checkExist(@RequestParam(value = "userId") String userId, @RequestParam(value = "keyword") String keyword) {
		Keyword result = keywordService.getByKeywordAndUserId(userId, keyword);
		return result;
	}
	
	@PostMapping("createKeyword")
	public Keyword createKeyword(@RequestParam(value = "keyword") String keyword, @RequestParam(value = "userId") String userId) {
		Keyword kw = new Keyword();
		kw.setKeyword(keyword);
		kw.setUserId(userId);
		kw.setAvailable(true);
		kw.setVersion(1);

		kw = keywordService.saveKeyword(kw);
		return kw;
	}
	
	@PostMapping("updateKeyword")
	public Keyword updateKeyword(@RequestParam(value = "keyword") String keyword, @RequestParam(value = "keywordId") int keywordId) {
		Keyword kw = keywordService.getKeywordById(keywordId);
		kw.setKeyword(keyword);
		kw.setVersion(kw.getVersion() + 1);
		kw = keywordService.saveKeyword(kw);
		return kw;
	}
	
	@PostMapping("deleteKeyword")
	public Keyword deleteKeyword(@RequestParam(value = "id") int id) {
		Keyword kw = keywordService.getKeywordById(id);
		if (kw != null) {
		kw.setAvailable(false);
		kw = keywordService.saveKeyword(kw);
		}
		return kw;
	}
	

}
