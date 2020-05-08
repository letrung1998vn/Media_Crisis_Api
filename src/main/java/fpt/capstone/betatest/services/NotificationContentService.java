package fpt.capstone.betatest.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.transaction.Transactional;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.NegativeRatio;
import fpt.capstone.betatest.entities.Notification_Content;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.EmailContentModel;
import fpt.capstone.betatest.model.EmailListContent;
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
	private LastStandardService lastStandardService;

	@Autowired
	private CheckMeaningService checkMeaningService;

	@Autowired
	private NegativeRatioService negativeRatioService;

	public final String detectTypeReact = "react";
	public final String detectTypeShare = "retweet";
	public final String detectTypeComment = "reply";
	public final String detectTypeIncreaseReact = "increaseReact";
	public final String detectTypeIncreaseShare = "increaseRetweet";
	public final String detectTypeIncreasComment = "increaseReply";
	public final String postType = "post";
	public final String commentType = "comment";

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
	public EmailListContent getEmailContentListPost(String keyword, String postId, String time) throws Exception {
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
		EmailListContent emailContent = new EmailListContent();
		List<NegativeRatio> lastNegativeRatio = negativeRatioService.getNegativeRatioByDateAsc(keyword, postType, date);
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(postId, ",");
		String id;
		List<Post> listPost = new ArrayList<>();
		List<String> listLinkDetail = new ArrayList<>();
		List<String> listRatio = new ArrayList<>();
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
				listLinkDetailModel.add(ldm);
			}
			for (int i = 0; i < listLinkDetailModel.size(); i++) {
				LinkDetailModel ldm = listLinkDetailModel.get(i);
				String linkDetail = ldm.getContent();
				linkDetail += "and||and";
				linkDetail += formatLinkDetail(ldm.getLink());
				linkDetail += "and||and";
				listLinkDetail.add(linkDetail);
			}
		}
		emailContent.setListContentAndLink(listLinkDetail);
		if (lastNegativeRatio.size() > 0) {
			for (int i = 0; i < lastNegativeRatio.size(); i++) {
				NegativeRatio lnr = lastNegativeRatio.get(i);
				String ratio = lnr.getRatio() + "and||and" + lnr.getUpdateDate();
				listRatio.add(ratio);
			}
		}
		emailContent.setListRatio(listRatio);
		return emailContent;
	}

	@Transactional
	public EmailListContent getEmailContentListComment(String keyword, String commentId, String time) throws Exception {
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
		EmailListContent emailContent = new EmailListContent();
		List<NegativeRatio> lastNegativeRatio = negativeRatioService.getNegativeRatioByDateAsc(keyword, commentType,
				date);
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(commentId, ",");
		String id;
		List<Comment> listComment = new ArrayList<>();
		List<String> listLinkDetail = new ArrayList<>();
		List<String> listRatio = new ArrayList<>();
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
				listLinkDetailModel.add(ldm);
			}
			for (int i = 0; i < listLinkDetailModel.size(); i++) {
				LinkDetailModel ldm = listLinkDetailModel.get(i);
				String linkDetail = ldm.getContent();
				linkDetail += "and||and";
				linkDetail += formatLinkDetail(ldm.getLink());
				linkDetail += "and||and";
				listLinkDetail.add(linkDetail);
			}
		}
		emailContent.setListContentAndLink(listLinkDetail);
		if (lastNegativeRatio.size() > 0) {
			for (int i = 0; i < lastNegativeRatio.size(); i++) {
				NegativeRatio lnr = lastNegativeRatio.get(i);
				String ratio = lnr.getRatio() + "and||and" + lnr.getUpdateDate();
				listRatio.add(ratio);
			}
		}
		emailContent.setListRatio(listRatio);
		return emailContent;

	}

	@Transactional
	public EmailContentModel getEmailContentList(String keyword, String Id) {
		EmailContentModel emailContent = new EmailContentModel();
		emailContent.setKeyword(keyword);
		StringTokenizer stk = new StringTokenizer(Id, ",");
		String id;
		List<Crisis> listCrisis = new ArrayList<>();
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
						LinkDetailModel ldm = new LinkDetailModel();
						Post post = result.get(0);
						ldm.setContent(post.getPostContent());
						ldm.setLink(post.getLinkDetail());
						ldm.setType(crisis.getDetectType());
						if (crisis.getDetectType().equals(detectTypeReact)) {
//							ldm.setStd(post_react_upper_limit);
							ldm.setNumber(post.getNumberOfReact());
						} else if (crisis.getDetectType().equals(detectTypeComment)) {
//							ldm.setStd(post_comment_upper_limit);
							ldm.setNumber(post.getNumberOfReply());
						} else if (crisis.getDetectType().equals(detectTypeShare)) {
//							ldm.setStd(post_share_upper_limit);
							ldm.setNumber(post.getNumberOfReweet());
						} else if (crisis.getDetectType().equals(detectTypeIncreaseReact)) {
							List<Post> postSorted = checkMeaningService.sortByCrawlDate(result);
							Post firstpost = postSorted.get(0);
							Post secondPost = postSorted.get(1);
							ldm.setContent(post.getPostContent());
							ldm.setLink(post.getLinkDetail());
							ldm.setType(crisis.getDetectType());
//							ldm.setStd(Increase_post_react_upper_limit);
							ldm.setNumber(firstpost.getNumberOfReact() - secondPost.getNumberOfReact());
						} else if (crisis.getDetectType().equals(detectTypeIncreasComment)) {
							List<Post> postSorted = checkMeaningService.sortByCrawlDate(result);
							Post firstpost = postSorted.get(0);
							Post secondPost = postSorted.get(1);
							ldm.setContent(post.getPostContent());
							ldm.setLink(post.getLinkDetail());
							ldm.setType(crisis.getDetectType());
//							ldm.setStd(Increase_post_comment_upper_limit);
							ldm.setNumber(firstpost.getNumberOfReply() - secondPost.getNumberOfReply());
						} else if (crisis.getDetectType().equals(detectTypeIncreaseShare)) {
							List<Post> postSorted = checkMeaningService.sortByCrawlDate(result);
							Post firstpost = postSorted.get(0);
							Post secondPost = postSorted.get(1);
							ldm.setContent(post.getPostContent());
							ldm.setLink(post.getLinkDetail());
							ldm.setType(crisis.getDetectType());
//							ldm.setStd(Increase_post_share_upper_limit);
							ldm.setNumber(firstpost.getNumberOfReweet() - secondPost.getNumberOfReweet());
						}
						listLinkDetailModel.add(ldm);
					}
				}
				if (crisis.getType().trim().equals("comment")) {
					List<Comment> result = commentService.getCommentByCommentIdSortCrawlDate(crisis.getContentId());
					if (result.size() > 0) {
						Comment comment = result.get(0);
						LinkDetailModel ldm = new LinkDetailModel();
						ldm.setContent(comment.getCommentContent());
						ldm.setLink(comment.getLinkDetail());
						ldm.setType(crisis.getDetectType());
						if (crisis.getDetectType().equals(detectTypeReact)) {
//							ldm.setStd(comment_react_upper_limit);
							ldm.setNumber(comment.getNumberOfReact());
						} else if (crisis.getDetectType().equals(detectTypeComment)) {
//							ldm.setStd(comment_comment_upper_limit);
							ldm.setNumber(comment.getNumberOfReply());
						} else if (crisis.getDetectType().equals(detectTypeIncreaseReact)) {
//							ldm.setStd(Increase_comment_react_upper_limit);
							List<Comment> ListComment = commentService
									.getCommentByCommentIdSortCrawlDate(comment.getCommentId());
							Comment firstComment = ListComment.get(0);
							Comment secondComment = ListComment.get(1);
							ldm.setNumber(firstComment.getNumberOfReact() - secondComment.getNumberOfReact());
						} else if (crisis.getDetectType().equals(detectTypeIncreasComment)) {
//							ldm.setStd(Increase_comment_comment_upper_limit);
							List<Comment> ListComment = commentService
									.getCommentByCommentIdSortCrawlDate(comment.getCommentId());
							Comment firstComment = ListComment.get(0);
							Comment secondComment = ListComment.get(1);
							ldm.setNumber(firstComment.getNumberOfReply() - secondComment.getNumberOfReply());
						}
						listLinkDetailModel.add(ldm);
					}
				}
			}
			if (listLinkDetailModel.size() > 0) {
				for (int i = 0; i < listLinkDetailModel.size(); i++) {
					LinkDetailModel ldm = listLinkDetailModel.get(i);
					String linkDetail = ldm.getContent();
					linkDetail += " and||and ";
					linkDetail += formatLinkDetail(ldm.getLink());
					linkDetail += " and||and ";
					linkDetail += ldm.getType();
					linkDetail += " and||and ";
					linkDetail += ldm.getStd();
					linkDetail += " and||and ";
					linkDetail += ldm.getNumber();
					listLinkDetail.add(linkDetail);
				}
			}
		}
		emailContent.setListLinkDetail(listLinkDetail);
		return emailContent;
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now().plusMinutes(10);
		emailContent += "&time=" + URLEncoder.encode(dtf.format(now), StandardCharsets.UTF_8.name());
		emailContent += "</h3>";
		return emailContent;
	}

	@Transactional
	public String createEmailLinkListComment(String keyword, List<Comment> listComment) throws Exception {
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now().plusMinutes(10);
		emailContent += "&time=" + URLEncoder.encode(dtf.format(now), StandardCharsets.UTF_8.name());
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
