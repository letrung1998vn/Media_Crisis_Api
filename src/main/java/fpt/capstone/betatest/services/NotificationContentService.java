package fpt.capstone.betatest.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.transaction.Transactional;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Notification_Content;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.EmailContentModel;
import fpt.capstone.betatest.model.LinkDetailModel;
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

	@Autowired
	private CheckMeaningService checkMeaningService;

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
		List<LinkDetailModel> listLinkDetailModel = new ArrayList<>();
		while (stk.hasMoreTokens()) {
			id = stk.nextToken();
			listPost.add(postService.getPostById(id));
		}
		if (listPost.size() > 0) {
			for (int i = 0; i < listPost.size(); i++) {
				Post post = listPost.get(i);
				LinkDetailModel ldm = new LinkDetailModel();
				ldm.setContent(post.getPostContent());
				ldm.setLink(post.getLinkDetail());
				ldm.setReason(getReasonPost(post));
				listLinkDetailModel.add(ldm);
			}
			for (int i = 0; i < listLinkDetailModel.size(); i++) {
				LinkDetailModel ldm = listLinkDetailModel.get(i);
				String linkDetail = ldm.getContent();
				linkDetail += "and||and";
				linkDetail += formatLinkDetail(ldm.getLink());
				linkDetail += "and||and";
				linkDetail += ldm.getReason();
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
		List<LinkDetailModel> listLinkDetailModel = new ArrayList<>();
		while (stk.hasMoreTokens()) {
			id = stk.nextToken();
			listComment.add(commentService.getCommentById(id));
		}
		if (listComment.size() > 0) {
			for (int i = 0; i < listComment.size(); i++) {
				Comment comment = listComment.get(i);
				LinkDetailModel ldm = new LinkDetailModel();
				ldm.setContent(comment.getCommentContent());
				ldm.setLink(comment.getLinkDetail());
				ldm.setReason(getReasonComment(comment));
				listLinkDetailModel.add(ldm);
			}
			for (int i = 0; i < listLinkDetailModel.size(); i++) {
				LinkDetailModel ldm = listLinkDetailModel.get(i);
				String linkDetail = ldm.getContent();
				linkDetail += "and||and";
				linkDetail += formatLinkDetail(ldm.getLink());
				linkDetail += "and||and";
				linkDetail += ldm.getReason();
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
		List<LinkDetailModel> listLinkDetailModel = new ArrayList<>();
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
					LinkDetailModel ldm = new LinkDetailModel();
					ldm.setContent(post.getPostContent());
					ldm.setLink(post.getLinkDetail());
					ldm.setReason(getReasonPost(post));
					listLinkDetailModel.add(ldm);
				}
				for (int i = 0; i < listLinkDetailModel.size(); i++) {
					LinkDetailModel ldm = listLinkDetailModel.get(i);
					String linkDetail = ldm.getContent();
					linkDetail += " and||and ";
					linkDetail += formatLinkDetail(ldm.getLink());
					linkDetail += " and||and ";
					linkDetail += ldm.getReason();
					listLinkDetail.add(linkDetail);
				}
			}
			if (listComment.size() > 0) {
				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					LinkDetailModel ldm = new LinkDetailModel();
					ldm.setContent(comment.getCommentContent());
					ldm.setLink(comment.getLinkDetail());
					ldm.setReason(getReasonComment(comment));
					listLinkDetailModel.add(ldm);
				}
				for (int i = 0; i < listLinkDetailModel.size(); i++) {
					LinkDetailModel ldm = listLinkDetailModel.get(i);
					String linkDetail = ldm.getContent();
					linkDetail += " and||and ";
					linkDetail += formatLinkDetail(ldm.getLink());
					linkDetail += " and||and ";
					linkDetail += ldm.getReason();
					listLinkDetail.add(linkDetail);
				}
			}
		}
		emailContent.setListLinkDetail(listLinkDetail);
		return emailContent;
	}

	private String getReasonPost(Post post) {
		String reason = "";
		float number_of_React = post.getNumberOfReact();
		float number_of_Retweet = post.getNumberOfReweet();
		float number_of_Reply = post.getNumberOfReply();
		if (number_of_React > number_of_Retweet && number_of_React > number_of_Reply) {
			reason = "Reach " + (int) number_of_React + " likes";
		} else if (number_of_Retweet > number_of_React && number_of_Retweet > number_of_Reply) {
			reason = "Reach " + (int) number_of_Retweet + " retweets";
		} else if (number_of_Reply > number_of_Retweet && number_of_Reply > number_of_React) {
			reason = "Reach " + (int) number_of_Reply + " replies";
		}
		return reason;
	}

	private String getReasonComment(Comment comment) {
		String reason = "";
		float number_of_React = comment.getNumberOfReact();
		float number_of_Reply = comment.getNumberOfReply();
		if (number_of_React > number_of_Reply) {
			reason = "Reach " + (int) number_of_React + " likes";
		} else if (number_of_Reply > number_of_React) {
			reason = "Reach " + (int) number_of_Reply + " replies";
		}
		return reason;
	}

	private String formatLinkDetail(String link) {
		link = link.replace("', '", "");
		link = link.replace("', ", "");
		link = link.replace("'", "");
		link = link.replace("(", "");
		link = link.replace(")", "");
		return link;
	}

	@Transactional
	public String createEmailLink(String keyword, List<Crisis> listCrisis) throws Exception {
		String emailContent;
		emailContent = "Here are crisis report for your keyword: <b>" + keyword + "</b>.<br/>";
		emailContent += "Click the link below to see the detail.<br/>";
		emailContent += "<h3>";
		emailContent += "http://localhost:8084/MediaCrisis_Demo/WebLinkContent";
		emailContent += "?keyword=";
		emailContent += URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
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
	public String createEmailLinkListPost(String keyword, List<Post> listPost) throws Exception {
		String emailContent;
		emailContent = "Here are list of negative post we found for your keyword: <b>" + keyword + "</b>.<br/>";
		emailContent += "Click the link below to see the detail.<br/>";
		emailContent += "<h3>";
		emailContent += "http://localhost:8084/MediaCrisis_Demo/WebLinkContentListPost";
		emailContent += "?keyword=";
		emailContent += URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
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
	public String createEmailLinkListComment(String keyword, List<Comment> listComment) throws Exception{
		String emailContent;
		emailContent = "Here are list of negative comment we found for your keyword: <b>" + keyword + "</b>.<br/>";
		emailContent += "Click the link below to see the detail.<br/>";
		emailContent += "<h3>";
		emailContent += "http://localhost:8084/MediaCrisis_Demo/WebLinkContentListComment";
		emailContent += "?keyword=";
		emailContent += URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
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
