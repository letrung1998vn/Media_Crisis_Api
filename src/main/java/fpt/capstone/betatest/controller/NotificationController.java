package fpt.capstone.betatest.controller;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fpt.capstone.betatest.model.EmailContentModel;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.NotificationContentService;
import fpt.capstone.betatest.services.NotificationService;
import fpt.capstone.betatest.services.NotificationTokenService;
import fpt.capstone.betatest.services.PostService;
import fpt.capstone.betatest.services.UserInfoService;
import fpt.capstone.betatest.services.UserService;

@RestController
@RequestMapping("/notification")
public class NotificationController {
	@Autowired
	PostService postService;
	@Autowired
	CommentService commentService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	NotificationContentService notificationContentService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	CrisisService crisisService;
	@Autowired
	UserService userService;
	@Autowired
	KeywordService keywordService;
	@Autowired
	NotificationTokenService notificationTokenService;

	@PostMapping("emailContentListPost")
	public EmailContentModel getEmailContentListPost(@RequestParam(name = "keyword") String keyword,
			@RequestParam(name = "post_id") String postId) {
		return notificationContentService.getEmailContentListPost(keyword, postId);
	}

	@PostMapping("emailContentListComment")
	public EmailContentModel getEmailContentListComment(@RequestParam(name = "keyword") String keyword,
			@RequestParam(name = "comment_id") String commentId) {
		return notificationContentService.getEmailContentListComment(keyword, commentId);
	}

	@PostMapping("emailContentList")
	public EmailContentModel getEmailContentList(@RequestParam(name = "keyword") String keyword,
			@RequestParam(name = "id") String Id) {
		return notificationContentService.getEmailContentList(keyword, Id);
	}
}
