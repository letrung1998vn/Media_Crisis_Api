package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Notification_Content;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.EmailContentModel;
import fpt.capstone.betatest.repositories.NotificationContentRepository;

@Service
public class NotificationContentService {
	@Autowired
	private NotificationContentRepository notificationContentRepository;

	@Autowired
	private PostService postService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private CrisisService crisisService;

	@Transactional
	public void getLinkDetailPost(List<Post> listPost, List<String> listLinkDetail) {
		for (int i = 0; i < listPost.size(); i++) {
			listLinkDetail.add(listPost.get(i).getLinkDetail());
		}
	}

	@Transactional
	public void getLinkDetailComment(List<Comment> listComment, List<String> listLinkDetail) {
		for (int i = 0; i < listComment.size(); i++) {
			listLinkDetail.add(listComment.get(i).getLinkDetail());
		}
	}

	@Transactional
	public void getLinkDetail(List<Post> listPost, List<Comment> listComment, List<String> listLinkDetail) {

		for (int i = 0; i < listComment.size(); i++) {
			listLinkDetail.add(listComment.get(i).getLinkDetail());
		}
		for (int i = 0; i < listPost.size(); i++) {
			listLinkDetail.add(listPost.get(i).getLinkDetail());
		}
	}

	@Transactional
	public Notification_Content save(Notification_Content notification_Content) {
		return notificationContentRepository.save(notification_Content);
	}

	@Transactional
	public List<Notification_Content> getNotificationContent(int notificationId) {
		return notificationContentRepository.findByNotificationId(notificationId);
	}

	@Transactional
	public EmailContentModel getEmailContentListPost(String keyword, String postId) {
		EmailContentModel emailContent = new EmailContentModel();
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(postId, ",");
		String id;
		List<Post> listPost = new ArrayList<>();
		List<String> listLinkDetail = new ArrayList<>();
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

	@Transactional
	public EmailContentModel getEmailContentListComment(String keyword, String commentId) {
		EmailContentModel emailContent = new EmailContentModel();
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(commentId, ",");
		String id;
		List<Comment> listComment = new ArrayList<>();
		List<String> listLinkDetail = new ArrayList<>();
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

	@Transactional
	public EmailContentModel getEmailContentList(String keyword, String Id) {
		EmailContentModel emailContent = new EmailContentModel();
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(Id, ",");
		String id;
		List<Crisis> listCrisis = new ArrayList<>();
		List<Comment> listComment = new ArrayList<>();
		List<Post> listPost = new ArrayList<>();
		List<String> listLinkDetail = new ArrayList<>();
		while (stk.hasMoreTokens()) {
			id = stk.nextToken();
			listCrisis.add(crisisService.getCrisisById(Integer.parseInt(id)));
		}
		if (listCrisis.size() > 0) {
			for (int i = 0; i < listCrisis.size(); i++) {
				Crisis crisis = listCrisis.get(i);
				if (crisis.getType().trim().equals("post")) {
					List<Post> result = postService.getPostByPostId(crisis.getContentId());
					if (result.size() > 0) {
						listPost.add(result.get(0));
					}
				}
				if (crisis.getType().trim().equals("comment")) {
					List<Comment> result = commentService.getCommentByCommentId(crisis.getContentId());
					if (result.size() > 0) {
						listComment.add(result.get(0));
					}
				}
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
		}
		emailContent.setListLinkDetail(listLinkDetail);
		return emailContent;
	}

	@Transactional
	public String createEmailLink(String keyword, List<Crisis> listCrisis) {
		String emailContent;
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

	@Transactional
	public String createEmailLinkListPost(String keyword, List<Post> listPost) {
		String emailContent;
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

	@Transactional
	public String createEmailLinkListComment(String keyword, List<Comment> listComment) {
		String emailContent;
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

	@Transactional
	public Notification_Content createEmailNotificationContent(int notificationId, int crisisId) {
		Notification_Content notiContent = new Notification_Content(crisisId, notificationId);
		Notification_Content content = this.save(notiContent);
		return content;
	}
}
