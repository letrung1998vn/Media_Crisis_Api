package fpt.capstone.betatest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
//	private KeywordRepository keywordsRepository;
	
	@GetMapping("getAll")
	public List<Keyword> getAll(@RequestParam(value = "userId") String userId) {
		List<Keyword> result = keywordService.getAll(userId);
		return result;
	}
	
	@GetMapping("searchKeyword")
	public List<Keyword> searchKeyword(@RequestParam(value = "keyword") String keyword, @RequestParam(value = "userId") String userId) {
		List<Keyword> result = keywordService.searchKeyword(userId, keyword);
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

		kw = keywordService.saveKeyword(kw);
		return kw;
	}
	
	@PostMapping("deleteKeyword")
	public void deleteKeyword(@RequestParam(value = "id") int id) {
		keywordService.deleteKeyword(id);
		
	}
	

}
