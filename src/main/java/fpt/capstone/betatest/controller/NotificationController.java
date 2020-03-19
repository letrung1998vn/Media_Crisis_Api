package fpt.capstone.betatest.controller;

import java.util.Date;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Notification;
import fpt.capstone.betatest.entities.Notification_Content;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
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
	
	private User user;
	List<Crisis> listCrisis;
	List<Post> listPost = new ArrayList<Post>();
	List<Comment> listComment = new ArrayList<Comment>();
	List<String> listLinkDetail = new ArrayList<String>();
	String emailContent;
	private static String host = "smtp.gmail.com";
	private static String username = "passmon2020@gmail.com";
	private static String password = "Vutiendat";
	private static String port = "587";
	
//	public NotificationController(User user, List<Crisis> listCrisis) {
//		super();
//		this.user = user;
//		this.listCrisis = listCrisis;
//	}
	public User getUserByUsername(String username) {
		user = userService.getUserByUsername(username);
		return user;
	}
	
	@PostMapping("sendEmail")
	public void sendEmailNotification(@RequestParam(value = "user_id") String username, @RequestParam(value = "keyword")String keyword) {
		User user = getUserByUsername(username);
		//get notification by user id
		Notification notiDTO = createEmailNotification(user);
		//get notification id
//		int notiId = getNotificationId(notiDTO);
		int notiId = notiDTO.getId();
		//get list crisis by keyword
		listCrisis = crisisService.getCrisisByKeyword(keyword);
		//tạo notification content
		for (int i = 0; i < listCrisis.size(); i++) {
			Notification_Content notiContent = createNotificationContent(notiId, listCrisis.get(i).getId());
//			Notification_Content notiContent = new Notification_Content(listCrisis.get(i).getId(), notiId);
		}
		//phân loại crisis: Post, Comment, Second Post, Second Comment
		classifyCrisisType(listCrisis);
		//lấy link detail trong crisis
		listLinkDetail = getLinkDetail();
		
		sendMail(username);
	}
	
	
	private void classifyCrisisType(List<Crisis> listCrisis) {
		
		for (int i = 0; i < listCrisis.size(); i++) {
			if (listCrisis.get(i).getType().trim().equals("post")) {
				Post post = postService.getPostById(listCrisis.get(i).getContentId());
				if (post != null) {
					listPost.add(post);
				}
			} else if (listCrisis.get(i).getType().trim().equals("comment")) {
				Comment comment = commentService.getCommentById(listCrisis.get(i).getContentId());
				if (comment != null) {
					listComment.add(comment);
				}
			}
		}
	}
	
	private Notification createEmailNotification (User user) {
		long millis = System.currentTimeMillis();  
		Date date = new Date(millis);
		Notification notificationDTO = new Notification(true, false, user, date);
		Notification noti = notificationService.save(notificationDTO);
		return noti;
	}
	
//	private int getNotificationId(Notification notification) {
//		return notificationService.getId(user, notification.isEmail(), notification.isWebhook(), notification.getDate());
//	}
	
	private Notification_Content createNotificationContent(int notificationId, int crisisId) {
		Notification_Content notiContent = new Notification_Content(crisisId, notificationId);
		Notification_Content content = notificationContentService.save(notiContent);
		return content;
	}
 
	private List<String> getLinkDetail() {
		
		for (int i = 0; i < listComment.size(); i++) {
			listLinkDetail.add(listComment.get(i).getLinkDetail());
		}
		for (int i = 0; i < listPost.size(); i++) {
			listLinkDetail.add(listPost.get(i).getLinkDetail());
		}
		return listLinkDetail;
	}
	
	private String createEmailContentWithLinkDetail() {
		emailContent = "Here are crisis's link detail.<br/>";
		emailContent += "Click to see more.<br/>";
		emailContent += "<h3>";
		for (String linkDetail : listLinkDetail) {
			linkDetail=linkDetail.replace("', '", "");
			linkDetail=linkDetail.replace("', ", "");
			linkDetail=linkDetail.replace("'", "");
			emailContent += linkDetail;
			emailContent += "<br/>";
		}
		emailContent += "</h3>";
		return emailContent;
	}
	
	private void sendMail(String userName) {
//		String message;
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
        
        //set Subject
        msg.setSubject("Crisis notification!");
        
        //set date
        msg.setSentDate(new java.util.Date());
        
        //set content
        String content = createEmailContentWithLinkDetail();
        msg.setContent(content, "text/html");
        
        //send email
        Transport.send(msg);
        System.out.print("Send successfully");
        } catch (AddressException e) {
        	e.printStackTrace();
        } catch (MessagingException ex) {
			ex.printStackTrace();
		}
        
	}
	
	
	
}
