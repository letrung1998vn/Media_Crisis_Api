package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.Keyword_Crawler;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.repositories.KeywordRepository;

@Service
public class KeywordService {
	@Autowired
	private KeywordRepository keywordsRepository;
	@Autowired
	private KeywordCrawlerService keywordCrawlerService;
	@Autowired
	private UserService userService;

	@Transactional
	public Page<Keyword> searchKeyword(String keyword, int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return keywordsRepository.findByKeywordContainingAndAvailable(keyword, page, true);
	}

	@Transactional
	public Page<Keyword> searchKeywordByUserAndKeywordContain(String keyword, User user, int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return keywordsRepository.findByUserAndKeywordContainingAndAvailable(user, keyword, page, true);
	}

	@Transactional
	public List<Keyword> getAllKeyword() {
		return keywordsRepository.findAll();
	}

	@Transactional
	public List<Keyword> getAll(User user) {
		return keywordsRepository.findByUser(user);
	}

	@Transactional
	public List<User> getAllUserHaveKeyword() {
		return keywordsRepository.findAllUserHaveKeyword();
	}

	@Transactional
	public Keyword saveKeyword(Keyword kw) {
		return keywordsRepository.save(kw);
	}

	@Transactional
	public MessageOutputModel createKeyword(User user, Keyword kw, String keyword, double crisisRate) {
		boolean havePermissionToCreate = false;
		MessageOutputModel mod = new MessageOutputModel();
		if (!(user.getRole().equals("user") && !user.isAvailable())) {
			havePermissionToCreate = true;
			List<Keyword> list = this.getAll(user);
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
			kw.setPercent_of_crisis(crisisRate);
			kw = this.saveKeyword(kw);
			mod.setStatusCode(2);
			mod.setStatusMessage("Created successfully!");
		}
		return mod;
	}

	@Transactional
	public MessageOutputModel updateKeyword(User user, Keyword kw, String keyword, int log_version, int keywordId, double crisisRate) {
		boolean havePermissionToUpdate = false;
		MessageOutputModel mod = new MessageOutputModel();
		if ((user.getUserName().equals(kw.getUser().getUserName())) || user.getRole().equals("admin")) {
			if (!(user.getRole().equals("user") && !user.isAvailable())) {
				havePermissionToUpdate = true;
				if (kw.getVersion() != log_version) {
					mod.setStatusCode(4);
					mod.setStatusMessage("Currently the value of this keyword has been changed to " + kw.getKeyword()
							+ ", please try again if you still want to update.");
					havePermissionToUpdate = false;
				} else {
					List<Keyword> list = this.getAll(kw.getUser());
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
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("You dont have permission to update this keyword!");
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
			kw.setPercent_of_crisis(crisisRate);
			this.saveKeyword(kw);
			mod.setStatusCode(2);
			mod.setStatusMessage("Update successfully!");
		}
		return mod;
	}

	@Transactional
	public MessageOutputModel deleteKeyword(User user, int log_version, int id) {
		boolean havePermissionToDelete = false;
		MessageOutputModel mod = new MessageOutputModel();
		Keyword kw = getKeywordById(id);
		if (kw != null) {
			if ((user.getUserName().equals(kw.getUser().getUserName())) || user.getRole().equals("admin")) {
				if (!(user.getRole().equals("user") && !user.isAvailable())) {
					havePermissionToDelete = true;
				} else {
					mod.setStatusCode(3);
					mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
				}
			} else {
				mod.setStatusCode(3);
				mod.setStatusMessage("You dont have permission to update this keyword!");
			}
		} else {
			mod.setStatusCode(4);
			mod.setStatusMessage("This keyword is no more existed!");
		}
		if (havePermissionToDelete) {
			if (kw.getVersion() == log_version) {
				this.deleteKeyword(kw);
				mod.setStatusCode(2);
				mod.setStatusMessage("Deleted successfully!");
			} else {
				mod.setStatusCode(4);
				mod.setStatusMessage("Your current keyword list is already old, please try again with the new one.");
			}
		}
		return mod;
	}

	@Transactional
	public Keyword getKeywordById(int id) {
		return keywordsRepository.findById(id);
	}

	@Transactional
	public Page<Keyword> getKeyword(String username, String keyword, int page) {
		Page<Keyword> result = null;
		if (username.equals("")) {
			result = this.searchKeyword(keyword, page);
		} else {
			User user = userService.getUserByUsername(username);
			if (user.isAvailable()) {
				result = this.searchKeywordByUserAndKeywordContain(keyword, user, page);
			}
		}
		return result;
	}

	@Transactional
	public void deleteKeyword(Keyword kw) {
		keywordsRepository.delete(kw);
	}

	@Transactional
	public List<String> getUsers() {
		List<String> listString = new ArrayList<String>();
		List<User> list = this.getAllUserHaveKeyword();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isAvailable()) {
				if (!listString.contains(list.get(i).getUserName())) {
					listString.add(list.get(i).getUserName());
				}
			}
		}
		return listString;
	}

	@Transactional
	public MessageOutputModel findAll() {
		MessageOutputModel mod = new MessageOutputModel();
		mod.setStatusCode(1);
		mod.setStatusMessage("message");
		List<Keyword> result = this.getAllKeyword();
		mod.setObj(result);
		return mod;
	}

	@Transactional
	public List<Keyword> getUserByKeyword(String keyword) {
		return keywordsRepository.findByKeyword(keyword);
	}

}
