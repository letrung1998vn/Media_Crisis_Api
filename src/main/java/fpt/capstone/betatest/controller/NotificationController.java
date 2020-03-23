package fpt.capstone.betatest.controller;

import java.util.Date;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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
import fpt.capstone.betatest.model.EmailContentModel;
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

	@GetMapping("test")
	public EmailContentModel getList() {
		listLinkDetail.add("abd");
		listLinkDetail.add("def");
		listLinkDetail.add("123");
		EmailContentModel test = new EmailContentModel();
		test.setListKeyWord(listLinkDetail);
		test.setKeyword("corona");
		return test;
	}

	@GetMapping("testcall")
	public void testCall() {
		listCrisis = new ArrayList<>();
		Crisis crisis = new Crisis();
		crisis.setId(47);
		crisis.setKeyword("corona");
		crisis.setContentId(new BigInteger("1238657550024871941"));
		crisis.setType("post");
		listCrisis.add(crisis);
		crisis = new Crisis();
		crisis.setId(48);
		crisis.setKeyword("corona");
		crisis.setContentId(new BigInteger("1238577362792591361"));
		crisis.setType("comment");
		listCrisis.add(crisis);
		sendEmailNotification(listCrisis, "corona", postService, commentService, notificationService,
				notificationContentService, userInfoService, crisisService, userService, keywordService);
	}

	@GetMapping("emailContent")
	public EmailContentModel getEmailContent(@RequestParam(name = "keyword") String keyword,
			@RequestParam(name = "id") String crisisId) {
		EmailContentModel emailContent = new EmailContentModel();
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(crisisId, ",");
		String id;
		listPost = new ArrayList<>();
		listComment = new ArrayList<>();
		listLinkDetail = new ArrayList<>();
		while (stk.hasMoreTokens()) {
			id = stk.nextToken();
			Crisis crisis = crisisService.getCrisisById(Integer.parseInt(id));
			classifyCrisisType(crisis, postService, commentService);
		}
		if (listPost.size() > 0) {
			for (int i = 0; i < listPost.size(); i++) {
				Post post = listPost.get(i);
				String linkDetail = post.getLinkDetail();
				linkDetail = linkDetail.replace("', '", "");
				linkDetail = linkDetail.replace("', ", "");
				linkDetail = linkDetail.replace("'", "");
				linkDetail = linkDetail.replace("(", "");
				linkDetail = linkDetail.replace(")", "");
				listLinkDetail.add(linkDetail);
			}
		}
		if (listComment.size() > 0) {
			for (int i = 0; i < listComment.size(); i++) {
				Comment comment = listComment.get(i);
				String linkDetail = comment.getLinkDetail();
				linkDetail = linkDetail.replace("', '", "");
				linkDetail = linkDetail.replace("', ", "");
				linkDetail = linkDetail.replace("'", "");
				linkDetail = linkDetail.replace("(", "");
				linkDetail = linkDetail.replace(")", "");
				listLinkDetail.add(linkDetail);
			}
		}
		emailContent.setListKeyWord(listLinkDetail);
		return emailContent;
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
			// lay link tu list crisis
			for (int i = 0; i < listcrisis.size(); i++) {
				Crisis crisis = listcrisis.get(i);
				classifyCrisisType(crisis, postService, commentService);
			}
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
				String sendEmailResult = sendMail(user.getUserName(), userInfoService, keyword, listcrisis);
				if (sendEmailResult.equals("OK")) {
					notificationDTO.setEmail(true);
					notificationService.save(notificationDTO);
				}
				String result = sendWebhook(user, keyword);
				if (result.equals("not concern")) {

				} else if (result.equals("OK!!!")) {
					notificationDTO.setWebhook(true);
					notificationService.save(notificationDTO);
				}
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
		Notification notificationDTO = new Notification(false, false, user, date);
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

	private String createEmailLink(String keyword, List<Crisis> listCrisis) {
		emailContent = "Here are crisis's link detail.<br/>";
		emailContent += "Click to see more.<br/>";
		emailContent += "<h3>";
		emailContent += "http://localhost:8181/notification/emailContent";
		emailContent += "?keyword=";
		emailContent += keyword;
		emailContent += "&id=";
		for (int i = 0; i < listCrisis.size(); i++) {
			emailContent += listCrisis.get(i).getId();
			if (i < listCrisis.size() - 1) {
				emailContent += ",";
			}
		}
		emailContent += "</h3>";
		return emailContent;
	}

	private String sendMail(String userName, UserInfoService userInfoService, String keyword, List<Crisis> listCrisis) {
		// String message;
		final String toAddress = userInfoService.getEmail(userName);
		String result = "false";
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
			String content = createEmailLink(keyword, listCrisis);
			msg.setContent(content, "text/html");

			// send email
			Transport.send(msg);
			result = "OK";
		} catch (AddressException e) {
			e.printStackTrace();
			result = "Wrong address";
		} catch (MessagingException ex) {
			ex.printStackTrace();
			result = "Wrong message";
		} finally {
			return result;
		}

	}

	private String createJsonStringWithLinkDetail(String keyword) {
		String json = "";
		json += "{\n";
		json += "    keyword: " + "\"" + keyword + "\"";
		json += ",\n";
		json += "    link: [\n";
		for (int i = 0; i < listLinkDetail.size(); i++) {
			String linkDetail = listLinkDetail.get(i);
			linkDetail = linkDetail.replace("', '", "");
			linkDetail = linkDetail.replace("', ", "");
			linkDetail = linkDetail.replace("'", "");
			json += "        \"";
			json += linkDetail;
			json += "\"";
			if (i < listLinkDetail.size() - 1) {
				json += ",";
				json += "\n";
			}
		}
		json += "\n";
		json += "    ]\n";
		json += "}\n";
		return json;
	}

	public String sendWebhook(User user, String keyword) {
		String url = user.getUser().getLink_webhook();
		if (!url.isEmpty()) {
			String jsonstring = createJsonStringWithLinkDetail(keyword);
			Webhook wh = new Webhook(url, jsonstring);
			return wh.connect();
		}
		return "not concern";
	}

}
