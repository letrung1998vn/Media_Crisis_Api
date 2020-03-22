package fpt.capstone.betatest.controller;

import java.util.Date;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.Notification;
import fpt.capstone.betatest.entities.Notification_Content;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.Webhook;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.NotificationContentService;
import fpt.capstone.betatest.services.NotificationService;
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

	private User user;
	List<Integer> listCrisisIdInCrisis = new ArrayList<Integer>();
	List<Integer> listCrisisIdInNotiContent = new ArrayList<Integer>();
	List<Notification_Content> listNotiContent;
	List<Notification> listNotification;
	List<Integer> listNotiId = new ArrayList<>();
	List<Crisis> listCrisis;
	List<Post> listPost = new ArrayList<Post>();
	List<Comment> listComment = new ArrayList<Comment>();
	List<String> listLinkDetail = new ArrayList<String>();
	List<String> listUserId = new ArrayList<String>();
	String emailContent;
	private static String host = "smtp.gmail.com";
	private static String username = "passmon2020@gmail.com";
	private static String password = "Vutiendat";
	private static String port = "587";
	
	@PostMapping("sendWebhook")
	public String sendWebhook(@RequestParam(value = "username") String username, @RequestParam(value = "jsonstring") String jsonstring) {
		User user = userService.getUserByUsername(username);
		String url = user.getUser().getLink_webhook();
		
		Webhook wh = new Webhook(url, jsonstring);
		return wh.connect();
	}
	
	public void sendEmailNotification(List<Crisis> listcrisis, String keyword, PostService postService,
			CommentService commentService, NotificationService notificationService,
			NotificationContentService notificationContentService, UserInfoService userInfoService,
			CrisisService crisisService, UserService userService, KeywordService keywordService) {
		listPost = new ArrayList<Post>();
		listComment = new ArrayList<Comment>();
		listLinkDetail = new ArrayList<>();
		// get list user by keyword
		List<User> listUser = new ArrayList<User>();
		List<Keyword> listKeyWord = keywordService.getUserByKeyword(keyword);
		for (int i = 0; i < listKeyWord.size(); i++) {
			Keyword keyword1 = listKeyWord.get(i);
			// get list user id
			listUser.add(userService.getUserByUsername(keyword1.getUser().getUserName()));
		}
		for (int i = 0; i < listcrisis.size(); i++) {
			Crisis crisis = listcrisis.get(i);
			// phân loại crisis:Post, Comment, Second Post, Second Comment
			classifyCrisisType(crisis, postService, commentService);
		}
		for (int i = 0; i < listUser.size(); i++) {
			List<Notification> listNoti = notificationService.getListNotification(listUser.get(i));
			for (int x = 0; x < listNoti.size(); x++) {
				List<Notification_Content> listNotiContent = notificationContentService
						.getNotificationContent(listNoti.get(x).getId());
				for (int y = 0; y < listNotiContent.size(); y++) {
					Notification_Content notiContent = listNotiContent.get(y);
					int result = checkCrisisIsSend(listcrisis, notiContent);
					if (result != -1) {
						listcrisis.remove(result);
					}
				}
			}
		}
		if (listcrisis.size() > 0) {
			// lấy link detail trong crisis
			getLinkDetail();
			for (int i = 0; i < listUser.size(); i++) {
				User user = listUser.get(i);
				String userID = user.getUserName();
				Notification notificationDTO = createEmailNotification(user, notificationService);
				int notiId = notificationDTO.getId();
				for (int x = 0; x < listcrisis.size(); x++) {
					Crisis crisis = listcrisis.get(x);
					Notification_Content notiContent = createEmailNotificationContent(notiId, crisis.getId(),
							notificationContentService);
				}
				// send mail
				sendMail(user.getUserName(), userInfoService);
			}
		}
	}

	private int checkCrisisIsSend(List<Crisis> listCrisis, Notification_Content notiContent) {
		for (int i = 0; i < listCrisis.size(); i++) {
			Crisis crisis = listCrisis.get(i);
			if (notiContent.getCrisisId() == crisis.getId()) {
				return i;
			}
		}
		return -1;
	}

	private void classifyCrisisType(Crisis crisis, PostService postService, CommentService commentService) {
		if (crisis.getType().trim().equals("post")) {
			List<Post> post = postService.getPostById(crisis.getContentId());
			if (post != null) {
				listPost.add(post.get(0));
			}
		} else if (crisis.getType().trim().equals("comment")) {
			List<Comment> comment = commentService.getCommentById(crisis.getContentId());
			if (comment != null) {
				listComment.add(comment.get(0));
			}
		}
	}

	private Notification createEmailNotification(User user, NotificationService notificationService) {
		long millis = System.currentTimeMillis();
		Date date = new Date(millis);
		Notification notificationDTO = new Notification(true, false, user, date);
		Notification noti = notificationService.save(notificationDTO);
		return noti;
	}

	private Notification_Content createEmailNotificationContent(int notificationId, int crisisId,
			NotificationContentService notificationContentService) {
		Notification_Content notiContent = new Notification_Content(crisisId, notificationId);
		Notification_Content content = notificationContentService.save(notiContent);
		return content;
	}

	private void getLinkDetail() {

		for (int i = 0; i < listComment.size(); i++) {
			listLinkDetail.add(listComment.get(i).getLinkDetail());
		}
		for (int i = 0; i < listPost.size(); i++) {
			listLinkDetail.add(listPost.get(i).getLinkDetail());
		}
	}

	private String createEmailContentWithLinkDetail() {
		emailContent = "Here are crisis's link detail.<br/>";
		emailContent += "Click to see more.<br/>";
		emailContent += "<h3>";
		for (String linkDetail : listLinkDetail) {
			linkDetail = linkDetail.replace("', '", "");
			linkDetail = linkDetail.replace("', ", "");
			linkDetail = linkDetail.replace("'", "");
			emailContent += linkDetail;
			emailContent += "<br/>";
		}
		emailContent += "</h3>";
		return emailContent;
	}

	private void sendMail(String userName, UserInfoService userInfoService) {
		// String message;
		final String toAddress = userInfoService.getEmail(userName);

		Properties properties = new Properties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");

		// creates a new session with an authenticator
		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		};

		Session session = Session.getInstance(properties, auth);

		// creates a new e-mail message
		Message msg = new MimeMessage(session);
		try {
			// Set From: header field of the header.
			msg.setFrom(new InternetAddress(username));

			// Set To: header field of the header.
			InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
			msg.setRecipients(Message.RecipientType.TO, toAddresses);

			// set Subject
			msg.setSubject("Crisis notification!");

			// set date
			msg.setSentDate(new java.util.Date());

			// set content
			String content = createEmailContentWithLinkDetail();
			msg.setContent(content, "text/html");

			// send email
			Transport.send(msg);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}

	}

}
