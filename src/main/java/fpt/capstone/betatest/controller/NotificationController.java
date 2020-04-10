package fpt.capstone.betatest.controller;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
import fpt.capstone.betatest.entities.NotificationToken;
import fpt.capstone.betatest.entities.Notification_Content;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.EmailContentModel;
import fpt.capstone.betatest.model.NotificationWebModel;
import fpt.capstone.betatest.model.Webhook;
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
		test.setListLinkDetail(listLinkDetail);
		test.setKeyword("corona");
		return test;
	}

	@GetMapping("testcall")
	public void testCall() {
		listCrisis = new ArrayList<>();
		Crisis crisis = new Crisis();
		crisis.setId(197);
		crisis.setKeyword("corona");
		crisis.setContentId(new BigInteger("1243733722916061185"));
		crisis.setType("post");
		listCrisis.add(crisis);
		crisis = new Crisis();
		crisis.setId(198);
		crisis.setKeyword("corona");
		crisis.setContentId(new BigInteger("1243733730021142529"));
		crisis.setType("comment");
		listCrisis.add(crisis);
		sendNotification(listCrisis, "corona", postService, commentService, notificationService,
				notificationContentService, userInfoService, crisisService, userService, keywordService,
				notificationTokenService);
	}

	@GetMapping("testcallListPost")
	public void testCallListPost() {
		listPost = new ArrayList<>();
		Post post = new Post();
		post.setId("00d20e20122a49e3b54aff0a9110e3c0");
		post.setPostId(new BigInteger("1244809374083801091"));
		post.setPostContent(
				"b'RT @sassymightyone: Texas is not testing people.\\nIf you have symptoms you are told to go home and isolate.\\nTexas Governor &amp; Lt. Governor ar\\xe2\\x80\\xa6'");
		String testDate = "2020-03-31 02:10:56.000";
		DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		Date date = null;
		try {
			date = formatter.parse(testDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		post.setCreateDate(date);
		post.setLinkDetail("('https://twitter.com/', 'AngelaBlueWave', '/status/', 1244809374083801091)");
		post.setNumberOfReact(2088);
		post.setNumberOfReweet(1183);
		post.setNumberOfReply(0);
		testDate = "2020-03-31 02:11:27.590";
		formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		date = null;
		try {
			date = formatter.parse(testDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		post.setCrawlDate(date);
		post.setKeyword("a");
		listPost.add(post);
		post = new Post();
		post.setId("013bd7320bc94701ad4feff172d47a47");
		post.setPostId(new BigInteger("1244809372854898689"));
		post.setPostContent(
				"b\"Well that was 2 weeks well spent. So much boredom but a good chance to get to know a pretty cool dude. Can't wait till next time bro!\"");
		testDate = "2020-03-31 02:10:56.000";
		formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		date = null;
		try {
			date = formatter.parse(testDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		post.setCreateDate(date);
		post.setLinkDetail("('https://twitter.com/', 'torque_00', '/status/', 1244809372854898689)");
		post.setNumberOfReact(0);
		post.setNumberOfReweet(0);
		post.setNumberOfReply(3);
		testDate = "2020-03-31 02:13:58.600";
		formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		date = null;
		try {
			date = formatter.parse(testDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		post.setCrawlDate(date);
		post.setKeyword("a");
		listPost.add(post);
		sendListPostNotification(listPost, "corona", postService, commentService, notificationService,
				notificationContentService, userInfoService, crisisService, userService, keywordService,
				notificationTokenService);
	}

	@GetMapping("testcallListComment")
	public void testCallListComment() {
		listComment = new ArrayList<>();
		Comment comment = new Comment();
		comment.setId("070f793d18a242cd8df573014a151b19");
		comment.setPostId("6a590e34de664b8bb14ed8b3bfa79719");
		comment.setCommentId(new BigInteger("1244809845661933568"));
		comment.setCommentContent(
				"b'@UncleBobsReason Ah gotcha. Tone is weird haha but ty anyway. Don\\xe2\\x80\\x99t want to make it seem like I should deserve way more.'");
		String testDate = "2020-03-31 02:12:49.000";
		DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		Date date = null;
		try {
			date = formatter.parse(testDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		comment.setCreateDate(date);
		comment.setLinkDetail("('https://twitter.com/', 'GenePark', '/status/', 1244809845661933568)");
		comment.setNumberOfReact(0);
		comment.setNumberOfReply(0);
		testDate = "2020-03-31 02:14:36.500";
		formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		date = null;
		try {
			date = formatter.parse(testDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		comment.setCrawlDate(date);
		listComment.add(comment);
		comment = new Comment();
		comment.setId("23b3d7de096040b588fe5183c3292984");
		comment.setPostId("7dceb272ee544e8fad2497c43b9a8195");
		comment.setCommentId(new BigInteger("1244809965346455552"));
		comment.setCommentContent("b'@BigDaddyEffy Absolutely'");
		testDate = "2020-03-31 02:13:17.000";
		formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		date = null;
		try {
			date = formatter.parse(testDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		comment.setCreateDate(date);
		comment.setLinkDetail("('https://twitter.com/', 'Treg2Cole', '/status/', 1244809965346455552)");
		comment.setNumberOfReact(0);
		comment.setNumberOfReply(1);
		testDate = "2020-03-31 02:26:13.593";
		formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		date = null;
		try {
			date = formatter.parse(testDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		comment.setCrawlDate(date);
		listComment.add(comment);
		sendListCommentNotification(listComment, "corona", postService, commentService, notificationService,
				notificationContentService, userInfoService, crisisService, userService, keywordService,
				notificationTokenService);
	}

	@PostMapping("emailContent")
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
		emailContent.setListLinkDetail(listLinkDetail);
		return emailContent;
	}

	@PostMapping("emailContentListPost")
	public EmailContentModel getEmailContentListPost(@RequestParam(name = "keyword") String keyword,
			@RequestParam(name = "post_id") String postId) {
		EmailContentModel emailContent = new EmailContentModel();
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(postId, ",");
		String id;
		listPost = new ArrayList<>();
		listLinkDetail = new ArrayList<>();
		while (stk.hasMoreTokens()) {
			id = stk.nextToken();
			listPost.add(postService.getPostById(id));
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
		emailContent.setListLinkDetail(listLinkDetail);
		return emailContent;
	}

	@PostMapping("emailContentListComment")
	public EmailContentModel getEmailContentListComment(@RequestParam(name = "keyword") String keyword,
			@RequestParam(name = "comment_id") String commentId) {
		EmailContentModel emailContent = new EmailContentModel();
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(commentId, ",");
		String id;
		listComment = new ArrayList<>();
		listLinkDetail = new ArrayList<>();
		while (stk.hasMoreTokens()) {
			id = stk.nextToken();
			listComment.add(commentService.getCommentById(id));
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
		emailContent.setListLinkDetail(listLinkDetail);
		return emailContent;
	}

	public void sendListPostNotification(List<Post> listPost, String keyword, PostService postService,
			CommentService commentService, NotificationService notificationService,
			NotificationContentService notificationContentService, UserInfoService userInfoService,
			CrisisService crisisService, UserService userService, KeywordService keywordService,
			NotificationTokenService notificationTokenService) {
		List<User> listUser = new ArrayList<User>();
		List<Keyword> listKeyWord = keywordService.getUserByKeyword(keyword);
		for (int i = 0; i < listKeyWord.size(); i++) {
			Keyword keyword1 = listKeyWord.get(i);
			// get list user id
			listUser.add(userService.getUserByUsername(keyword1.getUser().getUserName()));
		}
		for (int i = 0; i < listUser.size(); i++) {
			listLinkDetail = new ArrayList<>();
			User user = listUser.get(i);
			getLinkDetailPost(listPost);
			if (user.getRole().equals("user")) {
				try {
					sendEmailListPost(user.getUserName(), userInfoService, keyword, listPost);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sendWebhook(user, keyword);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sendNotificationListPost(user, keyword, notificationTokenService, listPost);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendListCommentNotification(List<Comment> listComment, String keyword, PostService postService,
			CommentService commentService, NotificationService notificationService,
			NotificationContentService notificationContentService, UserInfoService userInfoService,
			CrisisService crisisService, UserService userService, KeywordService keywordService,
			NotificationTokenService notificationTokenService) {
		List<User> listUser = new ArrayList<User>();
		List<Keyword> listKeyWord = keywordService.getUserByKeyword(keyword);
		for (int i = 0; i < listKeyWord.size(); i++) {
			Keyword keyword1 = listKeyWord.get(i);
			// get list user id
			listUser.add(userService.getUserByUsername(keyword1.getUser().getUserName()));
		}
		for (int i = 0; i < listUser.size(); i++) {
			listLinkDetail = new ArrayList<>();
			User user = listUser.get(i);
			getLinkDetailComment(listComment);
			if (user.getRole().equals("user")) {
				try {
					sendMailListComment(user.getUserName(), userInfoService, keyword, listComment);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sendWebhook(user, keyword);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sendNotificationListComment(user, keyword, notificationTokenService, listComment);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendNotification(List<Crisis> listcrisis, String keyword, PostService postService,
			CommentService commentService, NotificationService notificationService,
			NotificationContentService notificationContentService, UserInfoService userInfoService,
			CrisisService crisisService, UserService userService, KeywordService keywordService,
			NotificationTokenService notificationTokenService) {
		// get list user by keyword
		List<User> listUser = new ArrayList<User>();
		List<Keyword> listKeyWord = keywordService.getUserByKeyword(keyword);
		for (int i = 0; i < listKeyWord.size(); i++) {
			Keyword keyword1 = listKeyWord.get(i);
			// get list user id
			listUser.add(userService.getUserByUsername(keyword1.getUser().getUserName()));
		}
		for (int i = 0; i < listUser.size(); i++) {
			listPost = new ArrayList<Post>();
			listComment = new ArrayList<Comment>();
			listLinkDetail = new ArrayList<>();
			List<Crisis> sendCrisis = new ArrayList<>();
			sendCrisis.addAll(listcrisis);
			User user = listUser.get(i);
			if (user.getRole().equals("user")) {
				List<Notification> listNoti = notificationService.getListNotification(user);
				for (int x = 0; x < listNoti.size(); x++) {
					List<Notification_Content> listNotiContent = notificationContentService
							.getNotificationContent(listNoti.get(x).getId());
					for (int y = 0; y < listNotiContent.size(); y++) {
						Notification_Content notiContent = listNotiContent.get(y);
						int result = checkCrisisIsSend(sendCrisis, notiContent);
						if (result != -1) {
							sendCrisis.remove(result);
						}
					}
				}
				if (sendCrisis.size() > 0) {
					// lay link tu list crisis
					for (int x = 0; x < sendCrisis.size(); x++) {
						Crisis crisis = sendCrisis.get(x);
						classifyCrisisType(crisis, postService, commentService);
					}
					// lấy link detail trong crisis
					getLinkDetail();
					Notification notificationDTO = createEmailNotification(user, notificationService);
					int notiId = notificationDTO.getId();
					for (int z = 0; z < sendCrisis.size(); z++) {
						Crisis crisis = sendCrisis.get(z);
						createEmailNotificationContent(notiId, crisis.getId(), notificationContentService);
					}
					// send mail
					try {
						String sendEmailResult = sendMail(user.getUserName(), userInfoService, keyword, sendCrisis);
						if (sendEmailResult.equals("OK")) {
							notificationDTO.setEmail(true);
							notificationService.save(notificationDTO);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						String result = sendWebhook(user, keyword);
						if (result.equals("OK!!!")) {
							notificationDTO.setWebhook(true);
							notificationService.save(notificationDTO);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						sendNotification(user, keyword, notificationTokenService, sendCrisis);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
			List<Post> post = postService.getPostByPostId(crisis.getContentId());
			if (post != null && post.size() > 0) {
				listPost.add(post.get(0));
			}
		} else if (crisis.getType().trim().equals("comment")) {
			List<Comment> comment = commentService.getCommentByCommentId(crisis.getContentId());
			if (comment != null && comment.size() > 0) {
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

	private void getLinkDetailPost(List<Post> listPost) {
		for (int i = 0; i < listPost.size(); i++) {
			listLinkDetail.add(listPost.get(i).getLinkDetail());
		}
	}

	private void getLinkDetailComment(List<Comment> listComment) {
		for (int i = 0; i < listComment.size(); i++) {
			listLinkDetail.add(listComment.get(i).getLinkDetail());
		}
	}

	private String createEmailLink(String keyword, List<Crisis> listCrisis) {
		emailContent = "Here are crisis's link detail.<br/>";
		emailContent += "Click to see more.<br/>";
		emailContent += "<h3>";
		emailContent += "http://localhost:8084/MediaCrisis_Demo/WebLinkContent";
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

	private String createEmailLinkListPost(String keyword, List<Post> listPost) {
		emailContent = "Here are crisis's link detail.<br/>";
		emailContent += "Click to see more.<br/>";
		emailContent += "<h3>";
		emailContent += "http://localhost:8084/MediaCrisis_Demo/WebLinkContentListPost";
		emailContent += "?keyword=";
		emailContent += keyword;
		emailContent += "&post_id=";
		for (int i = 0; i < listPost.size(); i++) {
			emailContent += listPost.get(i).getId();
			if (i < listPost.size() - 1) {
				emailContent += ",";
			}
		}
		emailContent += "</h3>";
		return emailContent;
	}

	private String createEmailLinkListComment(String keyword, List<Comment> listComment) {
		emailContent = "Here are crisis's link detail.<br/>";
		emailContent += "Click to see more.<br/>";
		emailContent += "<h3>";
		emailContent += "http://localhost:8084/MediaCrisis_Demo/WebLinkContentListComment";
		emailContent += "?keyword=";
		emailContent += keyword;
		emailContent += "&comment_id=";
		for (int i = 0; i < listComment.size(); i++) {
			emailContent += listComment.get(i).getId();
			if (i < listComment.size() - 1) {
				emailContent += ",";
			}
		}
		emailContent += "</h3>";
		return emailContent;
	}

	private String sendEmailListPost(String userName, UserInfoService userInfoService, String keyword,
			List<Post> listPost) {
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
			String content = createEmailLinkListPost(keyword, listPost);
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

	private String sendMailListComment(String userName, UserInfoService userInfoService, String keyword,
			List<Comment> listComment) {
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
			String content = createEmailLinkListComment(keyword, listComment);
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

	private String createJsonNotificationWithLinkDetail(String notiToken, String keyword, List<Crisis> listCrisis) {
		String json = "";
		json += "{\n" + "  \"notification\": {\n" + "        \"title\": \"Notification for Keyword: " + keyword
				+ "\",\n" + "        \"body\": \"We have detect " + listCrisis.size() + " crisis about keyword: "
				+ keyword + "\",\n" + "        \"click_action\":\"WebLinkContent?keyword=" + keyword + "&id=";
		for (int i = 0; i < listCrisis.size(); i++) {
			json += listCrisis.get(i).getId();
			if (i < listCrisis.size() - 1) {
				json += ",";
			}
		}
		json += "\"\n   },\n" + "  \"to\": \"" + notiToken + "\"\n" + "}";
		return json;
	}

	private String createJsonNotificationWithLinkDetailListPost(String notiToken, String keyword, List<Post> listPost) {
		String json = "";
		json += "{\n" + "  \"notification\": {\n" + "        \"title\": \"Notification for Keyword: " + keyword
				+ "\",\n" + "        \"body\": \"We have detect abnormal increase post negative in keyword: " + keyword
				+ "\",\n" + "        \"click_action\":\"WebLinkContentListPost?keyword=" + keyword + "&post_id=";
		for (int i = 0; i < listPost.size(); i++) {
			json += listPost.get(i).getId();
			if (i < listPost.size() - 1) {
				json += ",";
			}
		}
		json += "\"\n   },\n" + "  \"to\": \"" + notiToken + "\"\n" + "}";
		return json;
	}

	private String createJsonNotificationWithLinkDetailListComment(String notiToken, String keyword,
			List<Comment> listComment) {
		String json = "";
		json += "{\n" + "  \"notification\": {\n" + "        \"title\": \"Notification for Keyword: " + keyword
				+ "\",\n" + "        \"body\": \"We have detect abnormal increase post negative in keyword: " + keyword
				+ "\",\n" + "        \"click_action\":\"WebLinkContentListComment?keyword=" + keyword + "&comment_id=";
		for (int i = 0; i < listComment.size(); i++) {
			json += listComment.get(i).getId();
			if (i < listComment.size() - 1) {
				json += ",";
			}
		}
		json += "\"\n   },\n" + "  \"to\": \"" + notiToken + "\"\n" + "}";
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

	public void sendNotification(User user, String keyword, NotificationTokenService notificationTokenService,
			List<Crisis> listCrisis) {
		List<NotificationToken> listNoti = notificationTokenService.getNotiTokenByUserId(user.getUserName());
		for (int i = 0; i < listNoti.size(); i++) {
			NotificationToken notiToken = listNoti.get(i);
			if (notiToken.isAvailable()) {
				String json = createJsonNotificationWithLinkDetail(notiToken.getNotiToken(), keyword, listCrisis);
				NotificationWebModel noti = new NotificationWebModel("https://fcm.googleapis.com/fcm/send", json);
				System.out.println("Status: " + noti.connect());
			}
		}
	}

	public void sendNotificationListPost(User user, String keyword, NotificationTokenService notificationTokenService,
			List<Post> listPost) {
		List<NotificationToken> listNoti = notificationTokenService.getNotiTokenByUserId(user.getUserName());
		for (int i = 0; i < listNoti.size(); i++) {
			NotificationToken notiToken = listNoti.get(i);
			if (notiToken.isAvailable()) {
				String json = createJsonNotificationWithLinkDetailListPost(notiToken.getNotiToken(), keyword, listPost);
				NotificationWebModel noti = new NotificationWebModel("https://fcm.googleapis.com/fcm/send", json);
				System.out.println("Status: " + noti.connect());
			}
		}
	}

	public void sendNotificationListComment(User user, String keyword,
			NotificationTokenService notificationTokenService, List<Comment> listComment) {
		List<NotificationToken> listNoti = notificationTokenService.getNotiTokenByUserId(user.getUserName());
		for (int i = 0; i < listNoti.size(); i++) {
			NotificationToken notiToken = listNoti.get(i);
			if (notiToken.isAvailable()) {
				String json = createJsonNotificationWithLinkDetailListComment(notiToken.getNotiToken(), keyword,
						listComment);
				NotificationWebModel noti = new NotificationWebModel("https://fcm.googleapis.com/fcm/send", json);
				System.out.println("Status: " + noti.connect());
			}
		}
	}
}
