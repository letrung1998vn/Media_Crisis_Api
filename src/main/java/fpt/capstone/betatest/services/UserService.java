package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.entities.UserInfo;
import fpt.capstone.betatest.model.CrisisModel;
import fpt.capstone.betatest.model.MessageOutputModel;
import fpt.capstone.betatest.model.UserCrisis;
import fpt.capstone.betatest.repositories.CrisisRepository;
import fpt.capstone.betatest.repositories.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository usersRepository;
	@Autowired
	private CrisisRepository crisisRepository;
	@Autowired
	private PostService postService;
	@Autowired
	private CommentService commentService;
	@Autowired
	KeywordService keywordService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	CrisisService crisisService;
	
	@Transactional
	public User checkLogin(String username, String password) {
		return usersRepository.findByUserNameAndPassword(username, password);
	}
	
	@Transactional
	public List<User> findAll() {
		return usersRepository.findAll();
	}

	@Transactional
	public boolean checkUserExist(String username) {
		boolean result = false;
		User user = usersRepository.findByUserName(username);
		if (user == null) {
			result = true;
		}
		return result;
	}
	
	@Transactional
	public User getUserByUsername(String username) {
		User user = usersRepository.findByUserName(username);
		return user;
	}
	
	@Transactional
	public User saveUser(User u) {
		return usersRepository.save(u);
	}
	
	@Transactional
	public Page<User> searchByUsernameAndPage(String userId, int Page) {
		Pageable page = PageRequest.of((Page - 1), 10);
		return usersRepository.findByUserNameContaining(userId, page);
	}
	
	@Transactional
	public MessageOutputModel changeUserStatus(String username) {
		User user = this.getUserByUsername(username);
		MessageOutputModel mod = new MessageOutputModel();
		if (user != null) {
			boolean isAvailable = user.isAvailable();
			List<Keyword> listKeyword = keywordService.getAll(user);
			if (isAvailable) {
				isAvailable = false;
				Keyword keyword;
				for (int i = 0; i < listKeyword.size(); i++) {
					keyword = listKeyword.get(i);
					keyword.setAvailable(false);
					keywordService.saveKeyword(keyword);
				}
			} else {
				isAvailable = true;
				Keyword keyword;
				for (int i = 0; i < listKeyword.size(); i++) {
					keyword = listKeyword.get(i);
					keyword.setAvailable(true);
					keywordService.saveKeyword(keyword);
				}
			}
			user.setAvailable(isAvailable);
			if (this.saveUser(user) != null) {
				mod.setStatusCode(2);
				mod.setStatusMessage("User status changed!");
			} else {
				mod.setStatusCode(4);
				mod.setStatusMessage("User status changed fail, please try again!");
			}
		} else {
			mod.setStatusCode(4);
			mod.setStatusMessage("This user is not exist anymore!");
		}
		return mod;
	}
	
	@Transactional
	public MessageOutputModel checkLogin(User result) {
		MessageOutputModel mod = new MessageOutputModel();
		if (result == null) {
			mod.setStatusCode(4);
			mod.setStatusMessage("Invalid username or password, please try again!");
		} else {
			if (result.isAvailable()) {
				mod.setStatusCode(2);
				mod.setStatusMessage("Welcome");
				result.setKeyword(null);
				mod.setObj(result);
			} else {
				mod.setStatusCode(3);

				mod.setStatusMessage(
						"Your account has been banned permanently, please contact admin for more information!");
			}
		}
		return mod;
	}
	
	@Transactional
	public MessageOutputModel updateWebhook(String link, String username) {
		User user = this.getUserByUsername(username);
		MessageOutputModel mod = new MessageOutputModel();
		if (user.isAvailable()) {
			user.getUser().setLink_webhook(link);
			this.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Webhook link is saved!");
			mod.setObj(user.getUser().getLink_webhook());
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more infomation!");
		}
		return mod;
	}
	
	@Transactional
	public MessageOutputModel findKeyword(User result) {
		MessageOutputModel mod = new MessageOutputModel();
		if (result.isAvailable()) {
			if (!result.getKeyword().isEmpty()) {
				mod.setObj(result.getKeyword());
				mod.setStatusCode(2);
				mod.setStatusMessage("Get keyword success.");
			} else {
				mod.setStatusCode(4);
				mod.setStatusMessage("Your keyword list is empty.");
			}

		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been banned permanently, please contact admin for more infomation!");
		}
		return mod;
	}
	@Transactional
	public MessageOutputModel findNoti(User result) {
		MessageOutputModel mod = new MessageOutputModel();
		if (result.isAvailable()) {
			mod.setStatusCode(2);
			mod.setStatusMessage("Get noti success");
			mod.setObj(result.getNotifications());
		} else {
			mod.setStatusCode(3);
			mod.setStatusMessage(
					"Your account has been banned permanently, please contact admin for more information!");
		}
		return mod;
	}
	
	@Transactional
	public MessageOutputModel registration(String username , String password, String name, String email) {
		MessageOutputModel mod = new MessageOutputModel();
		User user = new User();
		UserInfo userInfo = new UserInfo();
		if (this.checkUserExist(username)) {
			try {
				user.setUserName(username);
				user.setPassword(password);
				user.setRole("user");
				user.setAvailable(true);
				user = this.saveUser(user);
				userInfo.setUserId(username);
				userInfo.setName(name);
				userInfo.setEmail(email);
				userInfo.setLink_webhook("");
				userInfo = userInfoService.saveUser(userInfo);
				mod.setStatusCode(2);
				mod.setStatusMessage("Sign up successfully, please login!");
			} catch (Exception e) {
				mod.setStatusCode(4);
				mod.setStatusMessage(e.getMessage());
			}
		} else {
			mod.setStatusCode(4);
			mod.setStatusMessage("This username is existed, please pick another!");
		}
		return mod;
	}
	
	@Transactional
	public MessageOutputModel updateProfile(User user, String email, String name) {
		MessageOutputModel mod = new MessageOutputModel();
		if (!user.isAvailable()) {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		} else {
			user.getUser().setEmail(email);
			user.getUser().setName(name);
			user = this.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Changed userprofile successfully.");
		}

		return mod;
	}
	
	@Transactional
	public MessageOutputModel updatePassword(User user , String password) {
		MessageOutputModel mod = new MessageOutputModel();
		if (!user.isAvailable()) {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		} else {
			user.setPassword(password);
			user = this.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Changed password successfully.");
		}
		return mod;
	}
	
	@Transactional
	public MessageOutputModel disableWebhook(User user) {
		MessageOutputModel mod = new MessageOutputModel();
		if (!user.isAvailable()) {
			mod.setStatusCode(3);
			mod.setStatusMessage("Your account has been disabled. Please contact admin for more information!");
		} else {
			user.getUser().setLink_webhook("");
			user = this.saveUser(user);
			mod.setStatusCode(2);
			mod.setStatusMessage("Disable webhook notification successfully.");
		}
		return mod;
	}
	
	public List<CrisisModel> getAllUserCrisis(User user) {
		CrisisModel cm = new CrisisModel();
		List<Keyword> userKeywordList = keywordService.getAll(user);
		List<CrisisModel> crisisOutputModel = new ArrayList<CrisisModel>();
		for (int i = 0; i < userKeywordList.size(); i++) {
			List<Crisis> listCrisis = crisisRepository.findByKeywordOrderByDetectDateDesc(userKeywordList.get(i).getKeyword());
			for (Crisis crisis : listCrisis) {
				cm = new CrisisModel();
				cm.setId(crisis.getId());
				cm.setDetectType(crisis.getDetectType());
				cm.setPercentage(crisis.getPercentage());
				cm.setKeyword(crisis.getKeyword());
				cm.setDetectDate(crisis.getDetectDate());
				if(crisis.getType().trim().equals("post")) {
					cm.setType("post");
					//System.out.println(crisis.getContentId()+"");
					//System.out.println(postService.findPostById(crisis.getContentId()+""));
					cm.setContent(postService.findPostById(crisis.getContentId()).get(0).getPostContent());
				} else {
					cm.setType("comment");
					cm.setContent(commentService.getCommentByCommentId(crisis.getContentId()).get(0).getCommentContent());
				}
				crisisOutputModel.add(cm);
			}
		}
		return crisisOutputModel;}
}
