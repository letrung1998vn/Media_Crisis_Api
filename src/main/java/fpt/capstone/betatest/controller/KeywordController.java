package fpt.capstone.betatest.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.Keyword_Crawler;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.repositories.KeywordRepository;
import fpt.capstone.betatest.services.KeywordCrawlerService;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.UserInfoService;
import fpt.capstone.betatest.services.UserService;

@RestController
@RequestMapping("/keyword")
public class KeywordController {
	@Autowired
	KeywordService keywordService;
	@Autowired
	private UserService userService;
	@Autowired
	private KeywordCrawlerService keywordCrawlerService;

	@GetMapping("findAll")
	public MessageOutputModel findAll() {
		MessageOutputModel mod = new MessageOutputModel();
		mod.setStatusCode(1);
		mod.setStatusMessage("message");
		List<Keyword> result = keywordService.getAllKeyword();
		mod.setObj(result);
		return mod;
	}

	@GetMapping("getUsers")
	public List<String> getUsers() {
		List<String> listString = new ArrayList<String>();
		List<User> list = keywordService.getAllUserHaveKeyword();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isAvailable()) {
				if (!listString.contains(list.get(i).getUserName())) {
					listString.add(list.get(i).getUserName());
				}
			}
		}
		return listString;
	}

	@PostMapping("search")
	public Page<Keyword> getKeyword(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "username") String username, @RequestParam(value = "page") int page) {
		Page<Keyword> result = null;
		if (username.equals("")) {
			result = keywordService.searchKeyword(keyword, page);
		} else {
			User user = userService.getUserByUsername(username);
			if (user.isAvailable()) {
				result = keywordService.searchKeywordByUserAndKeywordContain(keyword, user, page);
			}
		}
		return result;
	}
//
//	@GetMapping("check")
//	public Keyword checkExist(@RequestParam(value = "userId") String userId,
//			@RequestParam(value = "keyword") String keyword) {
//		Keyword result = keywordService.getByKeywordAndUserId(userId, keyword);
//		return result;
//	}

	@PostMapping("createKeyword")
	public MessageOutputModel createKeyword(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "userId") String userId) {
		Keyword kw = new Keyword();
		MessageOutputModel mod = new MessageOutputModel();
		User user = userService.getUserByUsername(userId);
		boolean havePermissionToCreate = false;

		if (!(user.getRole().equals("user") && !user.isAvailable())) {
			havePermissionToCreate = true;
			List<Keyword> list = keywordService.getAll(user);
			if (user.getRole().equals("user")) {
				if (list.size() >= 10) {
					mod.setStatusCode(4);
					mod.setStatusMessage(
							"You have reached the limit for the number of keywords! Please contact admin for more infomation!");
					havePermissionToCreate = false;
				}
			}
			if (havePermissionToCreate) {
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).getKeyword().equals(keyword)) {
						mod.setStatusCode(4);
						mod.setStatusMessage("This keyword is existed!");
						havePermissionToCreate = false;
						break;
					}
				}
			}
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		}
		if (havePermissionToCreate) {
			boolean existed = keywordCrawlerService.checkExist(keyword);
			if (!existed) {
				Keyword_Crawler kc = new Keyword_Crawler();
				kc.setKeyword(keyword);
				keywordCrawlerService.saveKeyword(kc);
			}
			kw.setKeyword(keyword);
			kw.setUser(user);
			kw.setAvailable(true);
			kw.setVersion(1);
			kw = keywordService.saveKeyword(kw);
			mod.setStatusCode(2);
			mod.setStatusMessage("Created successfully!");
		}
		return mod;
	}

	@PostMapping("updateKeyword")
	public MessageOutputModel updateKeyword(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "keywordId") int keywordId, @RequestParam(value = "logVersion") int log_version,
			@RequestParam(value = "author") String author) {
		Keyword kw = keywordService.getKeywordById(keywordId);
		MessageOutputModel mod = new MessageOutputModel();
		User user = userService.getUserByUsername(author);
		boolean havePermissionToUpdate = false;

		if (!(user.getRole().equals("user") && !user.isAvailable())) {
			havePermissionToUpdate = true;
			if (kw.getVersion() != log_version) {
				mod.setStatusCode(4);
				mod.setStatusMessage("Currently the value of this keyword has been changed to " + kw.getKeyword()
						+ ", please try again if you still want to update.");
				havePermissionToUpdate = false;
			} else {
				List<Keyword> list = keywordService.getAll(kw.getUser());
				for (int i = 0; i < list.size(); i++) {
					if ((list.get(i).getKeyword().toLowerCase().equals(keyword.toLowerCase()))
							&& (list.get(i).getId() != keywordId) && (list.get(i).isAvailable())) {
						mod.setStatusCode(4);
						mod.setStatusMessage("This user already have this keyword!");
						havePermissionToUpdate = false;
					}
				}
			}
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		}
		if (havePermissionToUpdate) {
			boolean existed = keywordCrawlerService.checkExist(keyword);
			if (!existed) {
				Keyword_Crawler kc = new Keyword_Crawler();
				kc.setKeyword(keyword);
				keywordCrawlerService.saveKeyword(kc);
			}
			kw.setKeyword(keyword);
			kw.setVersion(kw.getVersion() + 1);
			keywordService.saveKeyword(kw);
			mod.setStatusCode(2);
			mod.setStatusMessage("Update successfully!");
		}
		return mod;
	}

	@PostMapping("deleteKeyword")
	public MessageOutputModel deleteKeyword(@RequestParam(value = "id") int id,
			@RequestParam(value = "logVersion") int log_version, @RequestParam(value = "author") String author) {

		MessageOutputModel mod = new MessageOutputModel();
		User user = userService.getUserByUsername(author);
		boolean havePermissionToDelete = false;

		if (!(user.getRole().equals("user") && !user.isAvailable())) {
			havePermissionToDelete = true;
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		}
		if (havePermissionToDelete) {
			Keyword kw = keywordService.getKeywordById(id);
			if (kw == null) {
				mod.setStatusCode(4);
				mod.setStatusMessage("This keyword is not exist anymore.");
			} else {
				if (kw.getVersion() == log_version) {
					keywordService.deleteKeyword(kw);
					mod.setStatusCode(2);
					mod.setStatusMessage("Deleted successfully!");
				} else {
					mod.setStatusCode(4);
					mod.setStatusMessage(
							"Your current keyword list is already old, please try again with the new one.");
				}
			}
		}
		return mod;
	}

}
