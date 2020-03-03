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
import fpt.capstone.betatest.repositories.KeywordRepository;
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

	@GetMapping("getUsers")
	public List<String> getUsers() {
		return keywordService.getAllUserHaveKeyword();
	}

	@PostMapping("search")
	public Page<Keyword> getKeyword(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "username") String username, @RequestParam(value = "page") int page) {
		Page<Keyword> result = null;
		if (username.equals("")) {
			result = keywordService.searchKeyword(keyword, page);
		} else {
			result = keywordService.searchKeywordByUserIdAndKeywordContain(keyword, username, page);
		}

		return result;
	}

	@GetMapping("check")
	public Keyword checkExist(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "keyword") String keyword) {
		Keyword result = keywordService.getByKeywordAndUserId(userId, keyword);
		return result;
	}

	@PostMapping("createKeyword")
	public MessageOutputModel createKeyword(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "userId") String userId) {
		Keyword kw = new Keyword();
		MessageOutputModel mod = new MessageOutputModel();
		User user = userService.getByUsername(userId);
		boolean havePermissionToDelete = false;

		if ((user.getRole().equals("user") && user.isAvailable()) || user.getRole().equals("admin")) {
			havePermissionToDelete = true;
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		}
		if (havePermissionToDelete) {
			kw.setKeyword(keyword);
			kw.setUserId(userId);
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
		User user = userService.getByUsername(author);
		boolean havePermissionToUpdate = false;

		if ((user.getRole().equals("user") && user.isAvailable()) || user.getRole().equals("admin")) {
			havePermissionToUpdate = true;
			List<Keyword> list = keywordService.getAll(kw.getUserId());
			for (int i = 0; i < list.size(); i++) {
				if ((list.get(i).getKeyword().equals(keyword)) && (list.get(i).getId() != keywordId)
						&& (list.get(i).isAvailable())) {
					mod.setStatusCode(4);
					mod.setStatusMessage("This user already have this keyword!");
					havePermissionToUpdate = false;
				}
			}
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		}
		if (havePermissionToUpdate) {
			if (kw.getVersion() == log_version) {
				kw.setKeyword(keyword);
				kw.setVersion(kw.getVersion() + 1);
				keywordService.saveKeyword(kw);
				mod.setStatusCode(2);
				mod.setStatusMessage("Update successfully!");
			} else {
				mod.setStatusCode(4);
				mod.setStatusMessage("Currently the value of this keyword has been changed to " + kw.getKeyword() +", please try again if you still want to update.");
			}
		}
		return mod;
	}

	@PostMapping("deleteKeyword")
	public MessageOutputModel deleteKeyword(@RequestParam(value = "id") int id,
			@RequestParam(value = "logVersion") int log_version, @RequestParam(value = "author") String author) {
		Keyword kw = keywordService.getKeywordById(id);
		MessageOutputModel mod = new MessageOutputModel();
		User user = userService.getByUsername(author);
		boolean havePermissionToDelete = false;

		if ((user.getRole().equals("user") && user.isAvailable()) || user.getRole().equals("admin")) {
			havePermissionToDelete = true;
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		}
		if (havePermissionToDelete) {
			if (kw.getVersion() == log_version) {
				kw.setAvailable(false);
				kw.setVersion(kw.getVersion() + 1);
				keywordService.saveKeyword(kw);
				mod.setStatusCode(2);
				mod.setStatusMessage("Deleted successfully!");
			} else {
				mod.setStatusCode(4);
				mod.setStatusMessage("Your current keyword list is already old, please try again with the new one.");
			}
		}
		return mod;
	}

}
