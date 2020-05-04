package fpt.capstone.betatest.services;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.Notification;
import fpt.capstone.betatest.entities.NotificationToken;
import fpt.capstone.betatest.entities.Notification_Content;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.model.NotificationWebModel;
import fpt.capstone.betatest.model.Webhook;
import fpt.capstone.betatest.repositories.NotificationRepository;

@Service
public class NotificationService {
	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private NotificationContentService notificationContentService;
	
	@Autowired
	private UserInfoService userInfoService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CrisisService crisisService;
	
	@Autowired
	private KeywordService keywordService;
	
	@Autowired
	private NotificationTokenService notificationTokenService;
	
	private static String host = "smtp.gmail.com";
	private static String username = "passmon2020@gmail.com";
	private static String password = "Vutiendat123!!!";
	private static String port = "587";

	
	
	@Transactional
	public Notification save(Notification notification) {
		return notificationRepository.save(notification);
	}
	
	@Transactional
	public int getId(User userName, boolean email, boolean webhook, Date date) {
		return notificationRepository.findByUserAndEmailAndWebhookAndDate(userName, email, webhook, date);
	}
	
	@Transactional
	public List<Notification> getListNotification(User userName) {
		return notificationRepository.findByUser(userName);
	}
	
	@Transactional
	public void sendListPostNotification(List<Post> listPost, String keyword) {
		List<User> listUser = new ArrayList<User>();
		List<Keyword> listKeyWord = keywordService.getUserByKeyword(keyword);
		for (int i = 0; i < listKeyWord.size(); i++) {
			Keyword keyword1 = listKeyWord.get(i);
			// get list user id
			listUser.add(userService.getUserByUsername(keyword1.getUser().getUserName()));
		}
		for (int i = 0; i < listUser.size(); i++) {
			List<String> listLinkDetail = new ArrayList<>();
			User user = listUser.get(i);
			notificationContentService.getLinkDetailPost(listPost, listLinkDetail);
			if (user.getRole().equals("user")) {
				try {
					sendEmailListPost(user.getUserName(), keyword, listPost);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sendWebhook(user, keyword, listLinkDetail);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sendNotificationListPost(user, keyword, listPost);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Transactional
	public String sendEmailListPost(String userName, String keyword, List<Post> listPost) {
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
			String content = notificationContentService.createEmailLinkListPost(keyword, listPost);
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

	@Transactional
	public String sendMail(String userName, String keyword, List<Crisis> listCrisis) {
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
			String content = notificationContentService.createEmailLink(keyword, listCrisis);
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

	@Transactional
	public String sendMailListComment(String userName, String keyword, List<Comment> listComment) {
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
			String content = notificationContentService.createEmailLinkListComment(keyword, listComment);
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

	@Transactional
	public String createJsonStringWithLinkDetail(String keyword, List<String> listLinkDetail) {
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

	@Transactional
	public String createJsonNotificationWithLinkDetail(String notiToken, String keyword, List<Crisis> listCrisis) {
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

	public String createJsonNotificationWithLinkDetailListPost(String notiToken, String keyword, List<Post> listPost) {
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

	@Transactional
	public String createJsonNotificationWithLinkDetailListComment(String notiToken, String keyword,
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

	@Transactional
	public String sendWebhook(User user, String keyword, List<String> listLinkDetail) {
		String url = user.getUser().getLink_webhook();
		if (!url.isEmpty()) {
			String jsonstring = createJsonStringWithLinkDetail(keyword, listLinkDetail);
			Webhook wh = new Webhook(url, jsonstring);
			return wh.connect();
		}
		return "not concern";
	}

	@Transactional
	public void sendNotification(User user, String keyword, List<Crisis> listCrisis) {
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

	@Transactional
	public void sendNotificationListPost(User user, String keyword, List<Post> listPost) {
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

	@Transactional
	public void sendNotificationListComment(User user, String keyword, List<Comment> listComment) {
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
	
	@Transactional
	public Notification createEmailNotification(User user) {
		long millis = System.currentTimeMillis();
		Date date = new Date(millis);
		Notification notificationDTO = new Notification(false, false, user, date);
		Notification noti = this.save(notificationDTO);
		return noti;
	}
	
	@Transactional
	public void sendListCommentNotification(List<Comment> listComment, String keyword) {
		List<User> listUser = new ArrayList<User>();
		List<Keyword> listKeyWord = keywordService.getUserByKeyword(keyword);
		for (int i = 0; i < listKeyWord.size(); i++) {
			Keyword keyword1 = listKeyWord.get(i);
			// get list user id
			listUser.add(userService.getUserByUsername(keyword1.getUser().getUserName()));
		}
		for (int i = 0; i < listUser.size(); i++) {
			List<String> listLinkDetail = new ArrayList<>();
			User user = listUser.get(i);
			notificationContentService.getLinkDetailComment(listComment, listLinkDetail);
			if (user.getRole().equals("user")) {
				try {
					sendMailListComment(user.getUserName(), keyword, listComment);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sendWebhook(user, keyword, listLinkDetail);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sendNotificationListComment(user, keyword, listComment);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Transactional
	public void sendNotification(List<Crisis> listcrisis, String keyword) {
		// get list user by keyword
		List<User> listUser = new ArrayList<User>();
		List<Keyword> listKeyWord = keywordService.getUserByKeyword(keyword);
		for (int i = 0; i < listKeyWord.size(); i++) {
			Keyword keyword1 = listKeyWord.get(i);
			// get list user id
			listUser.add(userService.getUserByUsername(keyword1.getUser().getUserName()));
		}
		for (int i = 0; i < listUser.size(); i++) {
			List<Post> listPost = new ArrayList<Post>();
			List<Comment> listComment = new ArrayList<Comment>();
			List<String> listLinkDetail = new ArrayList<>();
			List<Crisis> sendCrisis = new ArrayList<>();
			sendCrisis.addAll(listcrisis);
			User user = listUser.get(i);
			if (user.getRole().equals("user")) {
				List<Notification> listNoti = this.getListNotification(user);
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
						crisisService.classifyCrisisType(crisis, listPost, listComment);
					}
					// lấy link detail trong crisis
					notificationContentService.getLinkDetail(listPost, listComment, listLinkDetail);
					Notification notificationDTO = this.createEmailNotification(user);
					int notiId = notificationDTO.getId();
					for (int z = 0; z < sendCrisis.size(); z++) {
						Crisis crisis = sendCrisis.get(z);
						notificationContentService.createEmailNotificationContent(notiId, crisis.getId());
					}
					// send mail
					try {
						String sendEmailResult = sendMail(user.getUserName(), keyword, sendCrisis);
						if (sendEmailResult.equals("OK")) {
							notificationDTO.setEmail(true);
							this.save(notificationDTO);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						String result = sendWebhook(user, keyword, listLinkDetail);
						if (result.equals("OK!!!")) {
							notificationDTO.setWebhook(true);
							this.save(notificationDTO);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						sendNotification(user, keyword, sendCrisis);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Transactional
	public int checkCrisisIsSend(List<Crisis> listCrisis, Notification_Content notiContent) {
		for (int i = 0; i < listCrisis.size(); i++) {
			Crisis crisis = listCrisis.get(i);
			if (notiContent.getCrisisId() == crisis.getId()) {
				return i;
			}
		}
		return -1;
	}
	
	
}
