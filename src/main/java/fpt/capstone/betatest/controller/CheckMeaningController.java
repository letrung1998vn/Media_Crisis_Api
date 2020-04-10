
package fpt.capstone.betatest.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.EntityLevelSentimentParams;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Keyword_Crawler;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.NegativeRatio;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.BaseThread;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.KeywordCrawlerService;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.LastStandardService;
import fpt.capstone.betatest.services.NegativeRatioService;
import fpt.capstone.betatest.services.NotificationContentService;
import fpt.capstone.betatest.services.NotificationService;
import fpt.capstone.betatest.services.NotificationTokenService;
import fpt.capstone.betatest.services.PostService;
import fpt.capstone.betatest.services.UserInfoService;
import fpt.capstone.betatest.services.UserService;

@RestController
@RequestMapping("/checkMeaning")

public class CheckMeaningController {
	@Autowired
	PostService postService;
	@Autowired
	CommentService commentService;
	@Autowired
	CrisisService crisisService;
	@Autowired
	KeywordCrawlerService keywordCrawlerService;
	@Autowired
	KeywordService keywordService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	NotificationContentService notificationContentService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	UserService userService;
	@Autowired
	NotificationTokenService notificationTokenService;
	@Autowired
	NegativeRatioService negativeRatioService;
	@Autowired
	LastStandardService lastStandardService;

	@GetMapping("check")
	public void checkMeaning() throws Exception {
		TextAPIClient client = new TextAPIClient("43faa103", "f2aaee05b21dabe934b89bd3198801e8");
		CheckThread check = new CheckThread(client, crisisService, commentService, postService, keywordCrawlerService,
				keywordService, notificationService, notificationContentService, userInfoService, userService,
				notificationTokenService, negativeRatioService, lastStandardService);
		check.start();
	}

}

class CheckThread extends Thread {
	TextAPIClient client;
	CrisisService crisisService;
	CommentService commentService;
	PostService postService;
	KeywordCrawlerService keywordCrawlerService;
	KeywordService keywordService;
	NotificationService notificationService;
	NotificationContentService notificationContentService;
	UserInfoService userInfoService;
	UserService userService;
	NotificationTokenService notificationTokenService;
	NegativeRatioService negativeRatioService;
	final double diffenrentDate = 7;
	LastStandardService lastStandardService;

	public CheckThread(TextAPIClient client, CrisisService crisisService, CommentService commentService,
			PostService postService, KeywordCrawlerService keywordCrawlerService, KeywordService keywordService,
			NotificationService notificationService, NotificationContentService notificationContentService,
			UserInfoService userInfoService, UserService userService, NotificationTokenService notificationTokenService,
			NegativeRatioService negativeRatioService, LastStandardService lastStandardService) {
		this.client = client;
		this.crisisService = crisisService;
		this.commentService = commentService;
		this.postService = postService;
		this.keywordCrawlerService = keywordCrawlerService;
		this.keywordService = keywordService;
		this.notificationService = notificationService;
		this.notificationContentService = notificationContentService;
		this.userInfoService = userInfoService;
		this.userService = userService;
		this.notificationTokenService = notificationTokenService;
		this.negativeRatioService = negativeRatioService;
		this.lastStandardService = lastStandardService;
	}

	@Override
	public synchronized void start() {
		// Get all Keyword from keyword crawler
		List<Keyword_Crawler> listKeyword = keywordCrawlerService.getAllKeyword();
		List<Crisis> listCrisis = new ArrayList<>();
		for (int i = 0; i < listKeyword.size(); i++) {
			try {
				Keyword_Crawler keyword = listKeyword.get(i);
				calStandard(keyword.getKeyword());
				System.out.println("Keyword: " + keyword.getKeyword());
				listCrisis = new ArrayList<>();
				DetectCrisisInCurrent(listKeyword.get(i).getKeyword(), client, listCrisis);
				System.out.println("Size: " + listCrisis.size());
				for (int x = 0; x < listCrisis.size(); x++) {
					System.out.print("Keyword: " + listKeyword.get(i).getKeyword());
					System.out.println("Crisis" + x + ": " + listCrisis.get(x).getId());
				}
				if (listCrisis.size() > 0) {
					NotificationController notiController = new NotificationController();
					notiController.sendNotification(listCrisis, listKeyword.get(i).getKeyword(), postService,
							commentService, notificationService, notificationContentService, userInfoService,
							crisisService, userService, keywordService, notificationTokenService);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.interrupt();
	}

	private void calStandard(String keyword) {
		System.out.println("Keyword: " + keyword);
		List<Post> listPost = getNewPost(keyword);
		System.out.println("Post Size: " + listPost.size());
		List<Post> listFirstPost = new ArrayList<>();
		List<Post> listSencondPost = new ArrayList<>();
		List<Comment> listComment = new ArrayList<>();
		List<Comment> listFirstComment = new ArrayList<>();
		List<Comment> listSencondComment = new ArrayList<>();
		Post[] listStandard = new Post[listPost.size()];
		for (int i = 0; i < listPost.size(); i++) {
			Post post = listPost.get(i);
			listComment.addAll(commentService.getCommentByPostId(post.getId()));
		}
		for (int i = 0; i < listPost.size(); i++) {
			listStandard[i] = listPost.get(i);
		}
		calPostStandard(keyword, listPost);
		calCommentStandard(keyword, listComment);
		for (int i = 0; i < listStandard.length; i++) {
			Post post = listStandard[i];
			Post secondLastPost = postService.getSecondLastNewPost(post.getCrawlDate(), post.getPostId());
			if (secondLastPost == null) {
				listStandard[i] = null;
			} else {
				listSencondPost.add(secondLastPost);
			}
		}
		for (int i = 0; i < listStandard.length; i++) {
			if (listStandard[i] != null) {
				listFirstPost.add(listStandard[i]);
			}
		}
		for (int i = 0; i < listFirstPost.size(); i++) {
			Post post = listFirstPost.get(i);
			listFirstComment.addAll(commentService.getCommentByPostId(post.getId()));
		}
		for (int i = 0; i < listSencondPost.size(); i++) {
			Post secondPost = listSencondPost.get(i);
			List<Comment> listSecondCommentInDb = commentService.getCommentByPostId(secondPost.getId());
			for (int y = 0; y < listSecondCommentInDb.size(); y++) {
				Comment sencondCommentInDb = listSecondCommentInDb.get(i);
				if (checkCommentExist(listFirstComment, sencondCommentInDb)) {
					listSencondComment.add(sencondCommentInDb);
				}
			}
		}
		if (listFirstPost.size() > 0 && listSencondPost.size() > 0) {
			calIncreasePostStandard(keyword, listFirstPost, listSencondPost);
			calIncreaseCommentStandard(keyword, listFirstComment, listSencondComment);
		}
		for (int i = 0; i < listPost.size(); i++) {
			Post post = listPost.get(i);
			post.setNew(false);
			postService.save(post);
		}
	}

	private boolean checkCommentExist(List<Comment> listComment, Comment comment) {
		boolean result = false;
		for (int i = 0; i < listComment.size(); i++) {
			Comment com = listComment.get(i);
			if (com.getId().equals(comment.getId())) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void calCommentStandard(String keyword, List<Comment> listComment) {
		LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "comment", "react");
		LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "comment", "comment");
		if (lastCommentStandardReact != null && lastCommentStandardComment != null) {
			if (listComment.size() > 0) {
				double totalNewReactMean = 0;
				double totalNewCommentMean = 0;

				double totalNewReact = 0;
				double totalNewComment = 0;

				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					totalNewReact += comment.getNumberOfReact();
					totalNewComment += comment.getNumberOfReply();
				}

				double lastCommentReactVariance = Math.pow(lastCommentStandardReact.getLastStandard(), 2)
						* lastCommentStandardReact.getLastNumber();
				double lastCommentCommentVariance = Math.pow(lastCommentStandardComment.getLastStandard(), 2)
						* lastCommentStandardComment.getLastNumber();

				int totalNewCommentReact = lastCommentStandardReact.getLastNumber() + listComment.size();
				double tolalLastCommentReact = lastCommentStandardReact.getLastMean()
						* lastCommentStandardReact.getLastNumber();

				int totalNewCommentComment = lastCommentStandardComment.getLastNumber() + listComment.size();
				double tolalLastCommentComment = lastCommentStandardComment.getLastMean()
						* lastCommentStandardComment.getLastNumber();

				double reactMean = (tolalLastCommentReact + totalNewReact) / (totalNewCommentReact);

				double commentMean = (tolalLastCommentComment + totalNewComment) / (totalNewCommentComment);

				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					totalNewReactMean += (comment.getNumberOfReact() - reactMean)
							* (comment.getNumberOfReact() - lastCommentStandardReact.getLastMean());
					totalNewCommentMean += (comment.getNumberOfReply() - commentMean)
							* (comment.getNumberOfReply() - lastCommentStandardComment.getLastMean());
				}

				double commentReactVariance = lastCommentReactVariance + totalNewReactMean;
				double newCommentReactStandard = Math
						.sqrt((commentReactVariance) / (listComment.size() + lastCommentStandardReact.getLastNumber()));

				double postCommentVariance = lastCommentCommentVariance + totalNewCommentMean;
				double newCommentCommentStandard = Math.sqrt(
						(postCommentVariance) / (listComment.size() + lastCommentStandardComment.getLastNumber()));

				lastCommentStandardReact.setLastMean((float) reactMean);
				lastCommentStandardComment.setLastMean((float) commentMean);

				lastCommentStandardReact.setLastNumber(totalNewCommentReact);
				lastCommentStandardComment.setLastNumber(totalNewCommentComment);

				lastCommentStandardReact.setLastStandard((float) newCommentReactStandard);
				lastCommentStandardComment.setLastStandard((float) newCommentCommentStandard);

				lastStandardService.save(lastCommentStandardReact);
				lastStandardService.save(lastCommentStandardComment);
			}
		} else {
			if (listComment.size() > 0) {
				lastCommentStandardReact = new LastStandard();
				lastCommentStandardComment = new LastStandard();
				double reactArray[] = new double[listComment.size()];
				double commentArray[] = new double[listComment.size()];
				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					reactArray[i] = comment.getNumberOfReact();
					commentArray[i] = comment.getNumberOfReply();
				}
				double reactStandard = calculateSD(reactArray);
				double reactMean = mean(reactArray);

				double commentStandard = calculateSD(commentArray);
				double commentMean = mean(commentArray);

				lastCommentStandardReact.setKeyword(keyword);
				lastCommentStandardComment.setKeyword(keyword);

				lastCommentStandardReact.setLastMean((float) reactMean);
				lastCommentStandardComment.setLastMean((float) commentMean);

				lastCommentStandardReact.setLastNumber(listComment.size());
				lastCommentStandardComment.setLastNumber(listComment.size());

				lastCommentStandardReact.setLastStandard((float) reactStandard);
				lastCommentStandardComment.setLastStandard((float) commentStandard);

				lastCommentStandardReact.setNumberType("react");
				lastCommentStandardComment.setNumberType("comment");

				lastCommentStandardReact.setType("comment");
				lastCommentStandardComment.setType("comment");

				lastStandardService.save(lastCommentStandardReact);
				lastStandardService.save(lastCommentStandardComment);
			}
		}
	}

	private void calPostStandard(String keyword, List<Post> listPost) {
		LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "post", "react");
		LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "post", "share");
		LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "post", "comment");
		if (lastPostStandardReact != null && lastPostStandardShare != null && lastPostStandardComment != null) {
			if (listPost.size() > 0) {
				double totalNewReactMean = 0;
				double totalNewShareMean = 0;
				double totalNewCommentMean = 0;

				double totalNewReact = 0;
				double totalNewShare = 0;
				double totalNewComment = 0;
				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					totalNewReact += post.getNumberOfReact();
					totalNewShare += post.getNumberOfReweet();
					totalNewComment += post.getNumberOfReply();
				}
				double lastPostReactVariance = Math.pow(lastPostStandardReact.getLastStandard(), 2)
						* lastPostStandardReact.getLastNumber();
				double lastPostShareVariance = Math.pow(lastPostStandardShare.getLastStandard(), 2)
						* lastPostStandardShare.getLastNumber();
				double lastPostCommentVariance = Math.pow(lastPostStandardComment.getLastStandard(), 2)
						* lastPostStandardComment.getLastNumber();

				int totalNewPostReact = lastPostStandardReact.getLastNumber() + listPost.size();
				double tolalLastPostReact = lastPostStandardReact.getLastMean() * lastPostStandardReact.getLastNumber();

				int totalNewPostShare = lastPostStandardShare.getLastNumber() + listPost.size();
				double tolalLastPostShare = lastPostStandardShare.getLastMean() * lastPostStandardShare.getLastNumber();

				int totalNewPostComment = lastPostStandardComment.getLastNumber() + listPost.size();
				double tolalLastPostComment = lastPostStandardComment.getLastMean()
						* lastPostStandardComment.getLastNumber();

				double reactMean = (tolalLastPostReact + totalNewReact) / (totalNewPostReact);

				double shareMean = (tolalLastPostShare + totalNewShare) / (totalNewPostShare);

				double commentMean = (tolalLastPostComment + totalNewComment) / (totalNewPostComment);

				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					totalNewReactMean += (post.getNumberOfReact() - reactMean)
							* (post.getNumberOfReact() - lastPostStandardReact.getLastMean());
					totalNewShareMean += (post.getNumberOfReweet() - shareMean)
							* (post.getNumberOfReweet() - lastPostStandardReact.getLastMean());
					totalNewCommentMean += (post.getNumberOfReply() - commentMean)
							* (post.getNumberOfReply() - lastPostStandardComment.getLastMean());
				}

				double postReactVariance = lastPostReactVariance + totalNewReactMean;
				double newPostReactStandard = Math
						.sqrt((postReactVariance) / (listPost.size() + lastPostStandardReact.getLastNumber()));

				double postShareVariance = lastPostShareVariance + totalNewShareMean;
				double newPostShareStandard = Math
						.sqrt((postShareVariance) / (listPost.size() + lastPostStandardReact.getLastNumber()));

				double postCommentVariance = lastPostCommentVariance + totalNewCommentMean;
				double newPostCommentStandard = Math
						.sqrt((postCommentVariance) / (listPost.size() + lastPostStandardComment.getLastNumber()));

				lastPostStandardReact.setLastMean((float) reactMean);
				lastPostStandardShare.setLastMean((float) shareMean);
				lastPostStandardComment.setLastMean((float) commentMean);

				lastPostStandardReact.setLastNumber(totalNewPostReact);
				lastPostStandardShare.setLastNumber(totalNewPostShare);
				lastPostStandardComment.setLastNumber(totalNewPostComment);

				lastPostStandardReact.setLastStandard((float) newPostReactStandard);
				lastPostStandardShare.setLastStandard((float) newPostShareStandard);
				lastPostStandardComment.setLastStandard((float) newPostCommentStandard);

				lastStandardService.save(lastPostStandardReact);
				lastStandardService.save(lastPostStandardShare);
				lastStandardService.save(lastPostStandardComment);
			}
		} else {
			if (listPost.size() > 0) {
				lastPostStandardReact = new LastStandard();
				lastPostStandardComment = new LastStandard();
				lastPostStandardShare = new LastStandard();
				double reactArray[] = new double[listPost.size()];
				double shareArray[] = new double[listPost.size()];
				double commentArray[] = new double[listPost.size()];
				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					reactArray[i] = post.getNumberOfReact();
					shareArray[i] = post.getNumberOfReweet();
					commentArray[i] = post.getNumberOfReply();
				}
				double reactStandard = calculateSD(reactArray);
				double reactMean = mean(reactArray);

				double shareStandard = calculateSD(shareArray);
				double shareMean = mean(shareArray);

				double commentStandard = calculateSD(commentArray);
				double commentMean = mean(commentArray);

				lastPostStandardReact.setKeyword(keyword);
				lastPostStandardShare.setKeyword(keyword);
				lastPostStandardComment.setKeyword(keyword);

				lastPostStandardReact.setLastMean((float) reactMean);
				lastPostStandardShare.setLastMean((float) shareMean);
				lastPostStandardComment.setLastMean((float) commentMean);

				lastPostStandardReact.setLastNumber(listPost.size());
				lastPostStandardShare.setLastNumber(listPost.size());
				lastPostStandardComment.setLastNumber(listPost.size());

				lastPostStandardReact.setLastStandard((float) reactStandard);
				lastPostStandardShare.setLastStandard((float) shareStandard);
				lastPostStandardComment.setLastStandard((float) commentStandard);

				lastPostStandardReact.setNumberType("react");
				lastPostStandardShare.setNumberType("share");
				lastPostStandardComment.setNumberType("comment");

				lastPostStandardReact.setType("post");
				lastPostStandardShare.setType("post");
				lastPostStandardComment.setType("post");

				lastStandardService.save(lastPostStandardReact);
				lastStandardService.save(lastPostStandardShare);
				lastStandardService.save(lastPostStandardComment);
			}
		}
	}

	private void calIncreaseCommentStandard(String keyword, List<Comment> listComment,
			List<Comment> listSecondComment) {
		LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "increaseComment",
				"react");
		LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "increaseComment",
				"comment");
		if (lastCommentStandardReact != null && lastCommentStandardComment != null) {
			if (listComment.size() > 0) {
				double totalNewReactMean = 0;
				double totalNewCommentMean = 0;

				double totalNewReact = 0;
				double totalNewComment = 0;

				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					int pos = getSameCommentPos(listSecondComment, comment);
					Comment secondComment = listSecondComment.get(pos);
					totalNewReact += comment.getNumberOfReact() - secondComment.getNumberOfReact();
					totalNewComment += comment.getNumberOfReply() - secondComment.getNumberOfReply();
				}

				double lastCommentReactVariance = Math.pow(lastCommentStandardReact.getLastStandard(), 2)
						* lastCommentStandardReact.getLastNumber();
				double lastCommentCommentVariance = Math.pow(lastCommentStandardComment.getLastStandard(), 2)
						* lastCommentStandardComment.getLastNumber();

				int totalNewCommentReact = lastCommentStandardReact.getLastNumber() + listComment.size();
				double tolalLastCommentReact = lastCommentStandardReact.getLastMean()
						* lastCommentStandardReact.getLastNumber();

				int totalNewCommentComment = lastCommentStandardComment.getLastNumber() + listComment.size();
				double tolalLastCommentComment = lastCommentStandardComment.getLastMean()
						* lastCommentStandardComment.getLastNumber();

				double reactMean = (tolalLastCommentReact + totalNewReact) / (totalNewCommentReact);

				double commentMean = (tolalLastCommentComment + totalNewComment) / (totalNewCommentComment);

				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					Comment secondComment = listSecondComment.get(i);
					double increaseReact = comment.getNumberOfReact() - secondComment.getNumberOfReact();
					double increaseComment = comment.getNumberOfReply() - secondComment.getNumberOfReply();
					totalNewReactMean += (increaseReact - reactMean)
							* (increaseReact - lastCommentStandardReact.getLastMean());
					totalNewCommentMean += (increaseComment - commentMean)
							* (increaseComment - lastCommentStandardComment.getLastMean());
				}

				double commentReactVariance = lastCommentReactVariance + totalNewReactMean;
				double newCommentReactStandard = Math
						.sqrt((commentReactVariance) / (listComment.size() + lastCommentStandardReact.getLastNumber()));

				double postCommentVariance = lastCommentCommentVariance + totalNewCommentMean;
				double newCommentCommentStandard = Math.sqrt(
						(postCommentVariance) / (listComment.size() + lastCommentStandardComment.getLastNumber()));

				lastCommentStandardReact.setLastMean((float) reactMean);
				lastCommentStandardComment.setLastMean((float) commentMean);

				lastCommentStandardReact.setLastNumber(totalNewCommentReact);
				lastCommentStandardComment.setLastNumber(totalNewCommentComment);

				lastCommentStandardReact.setLastStandard((float) newCommentReactStandard);
				lastCommentStandardComment.setLastStandard((float) newCommentCommentStandard);

				lastStandardService.save(lastCommentStandardReact);
				lastStandardService.save(lastCommentStandardComment);
			}
		} else {
			if (listComment.size() > 0) {
				lastCommentStandardReact = new LastStandard();
				lastCommentStandardComment = new LastStandard();
				double reactArray[] = new double[listComment.size()];
				double commentArray[] = new double[listComment.size()];
				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					int pos = getSameCommentPos(listSecondComment, comment);
					Comment secondComment = listSecondComment.get(pos);
					reactArray[i] = comment.getNumberOfReact() - secondComment.getNumberOfReact();
					commentArray[i] = comment.getNumberOfReply() - secondComment.getNumberOfReply();
				}
				double reactStandard = calculateSD(reactArray);
				double reactMean = mean(reactArray);

				double commentStandard = calculateSD(commentArray);
				double commentMean = mean(commentArray);

				lastCommentStandardReact.setKeyword(keyword);
				lastCommentStandardComment.setKeyword(keyword);

				lastCommentStandardReact.setLastMean((float) reactMean);
				lastCommentStandardComment.setLastMean((float) commentMean);

				lastCommentStandardReact.setLastNumber(listComment.size());
				lastCommentStandardComment.setLastNumber(listComment.size());

				lastCommentStandardReact.setLastStandard((float) reactStandard);
				lastCommentStandardComment.setLastStandard((float) commentStandard);

				lastCommentStandardReact.setNumberType("react");
				lastCommentStandardComment.setNumberType("comment");

				lastCommentStandardReact.setType("increaseComment");
				lastCommentStandardComment.setType("increaseComment");

				lastStandardService.save(lastCommentStandardReact);
				lastStandardService.save(lastCommentStandardComment);
			}
		}
	}

	private int getSameCommentPos(List<Comment> listComment, Comment comment) {
		int result = -1;
		for (int i = 0; i < listComment.size(); i++) {
			Comment com = listComment.get(i);
			if (com.getCommentId().equals(comment.getCommentId())) {
				result = i;
				break;
			}
		}
		return result;
	}

	private void calIncreasePostStandard(String keyword, List<Post> listPost, List<Post> listSecondPost) {
		LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "increasePost", "react");
		LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "increasePost", "share");
		LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "increasePost", "comment");

		if (lastPostStandardReact != null && lastPostStandardShare != null && lastPostStandardComment != null) {
			if (listPost.size() > 0) {
				double totalNewReactMean = 0;
				double totalNewShareMean = 0;
				double totalNewCommentMean = 0;

				double totalNewReact = 0;
				double totalNewShare = 0;
				double totalNewComment = 0;
				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					Post secondPost = listSecondPost.get(i);
					totalNewReact += post.getNumberOfReact() - secondPost.getNumberOfReact();
					totalNewShare += post.getNumberOfReweet() - secondPost.getNumberOfReweet();
					totalNewComment += post.getNumberOfReply() - secondPost.getNumberOfReply();
				}
				double lastPostReactVariance = Math.pow(lastPostStandardReact.getLastStandard(), 2)
						* lastPostStandardReact.getLastNumber();
				double lastPostShareVariance = Math.pow(lastPostStandardShare.getLastStandard(), 2)
						* lastPostStandardShare.getLastNumber();
				double lastPostCommentVariance = Math.pow(lastPostStandardComment.getLastStandard(), 2)
						* lastPostStandardComment.getLastNumber();

				int totalNewPostReact = lastPostStandardReact.getLastNumber() + listPost.size();
				double tolalLastPostReact = lastPostStandardReact.getLastMean() * lastPostStandardReact.getLastNumber();

				int totalNewPostShare = lastPostStandardShare.getLastNumber() + listPost.size();
				double tolalLastPostShare = lastPostStandardShare.getLastMean() * lastPostStandardShare.getLastNumber();

				int totalNewPostComment = lastPostStandardComment.getLastNumber() + listPost.size();
				double tolalLastPostComment = lastPostStandardComment.getLastMean()
						* lastPostStandardComment.getLastNumber();

				double reactMean = (tolalLastPostReact + totalNewReact) / (totalNewPostReact);

				double shareMean = (tolalLastPostShare + totalNewShare) / (totalNewPostShare);

				double commentMean = (tolalLastPostComment + totalNewComment) / (totalNewPostComment);

				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					Post secondPost = listSecondPost.get(i);
					double increaseReact = post.getNumberOfReact() - secondPost.getNumberOfReact();
					double increaseShare = post.getNumberOfReweet() - secondPost.getNumberOfReweet();
					double increaseComment = post.getNumberOfReply() - secondPost.getNumberOfReply();
					totalNewReactMean += (increaseReact - reactMean)
							* (increaseReact - lastPostStandardReact.getLastMean());
					totalNewShareMean += (increaseShare - shareMean)
							* (increaseShare - lastPostStandardReact.getLastMean());
					totalNewCommentMean += (increaseComment - commentMean)
							* (increaseComment - lastPostStandardComment.getLastMean());
				}

				double postReactVariance = lastPostReactVariance + totalNewReactMean;
				double newPostReactStandard = Math
						.sqrt((postReactVariance) / (listPost.size() + lastPostStandardReact.getLastNumber()));

				double postShareVariance = lastPostShareVariance + totalNewShareMean;
				double newPostShareStandard = Math
						.sqrt((postShareVariance) / (listPost.size() + lastPostStandardReact.getLastNumber()));

				double postCommentVariance = lastPostCommentVariance + totalNewCommentMean;
				double newPostCommentStandard = Math
						.sqrt((postCommentVariance) / (listPost.size() + lastPostStandardComment.getLastNumber()));

				lastPostStandardReact.setLastMean((float) reactMean);
				lastPostStandardShare.setLastMean((float) shareMean);
				lastPostStandardComment.setLastMean((float) commentMean);

				lastPostStandardReact.setLastNumber(totalNewPostReact);
				lastPostStandardShare.setLastNumber(totalNewPostShare);
				lastPostStandardComment.setLastNumber(totalNewPostComment);

				lastPostStandardReact.setLastStandard((float) newPostReactStandard);
				lastPostStandardShare.setLastStandard((float) newPostShareStandard);
				lastPostStandardComment.setLastStandard((float) newPostCommentStandard);

				lastStandardService.save(lastPostStandardReact);
				lastStandardService.save(lastPostStandardShare);
				lastStandardService.save(lastPostStandardComment);
			}
		} else {
			if (listPost.size() > 0) {
				lastPostStandardReact = new LastStandard();
				lastPostStandardComment = new LastStandard();
				lastPostStandardShare = new LastStandard();
				double reactArray[] = new double[listPost.size()];
				double shareArray[] = new double[listPost.size()];
				double commentArray[] = new double[listPost.size()];
				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					Post secondPost = listSecondPost.get(i);
					reactArray[i] = post.getNumberOfReact() - secondPost.getNumberOfReact();
					shareArray[i] = post.getNumberOfReweet() - secondPost.getNumberOfReweet();
					commentArray[i] = post.getNumberOfReply() - secondPost.getNumberOfReply();
				}
				double reactStandard = calculateSD(reactArray);
				double reactMean = mean(reactArray);

				double shareStandard = calculateSD(shareArray);
				double shareMean = mean(shareArray);

				double commentStandard = calculateSD(commentArray);
				double commentMean = mean(commentArray);

				lastPostStandardReact.setKeyword(keyword);
				lastPostStandardShare.setKeyword(keyword);
				lastPostStandardComment.setKeyword(keyword);

				lastPostStandardReact.setLastMean((float) reactMean);
				lastPostStandardShare.setLastMean((float) shareMean);
				lastPostStandardComment.setLastMean((float) commentMean);

				lastPostStandardReact.setLastNumber(listPost.size());
				lastPostStandardShare.setLastNumber(listPost.size());
				lastPostStandardComment.setLastNumber(listPost.size());

				lastPostStandardReact.setLastStandard((float) reactStandard);
				lastPostStandardShare.setLastStandard((float) shareStandard);
				lastPostStandardComment.setLastStandard((float) commentStandard);

				lastPostStandardReact.setNumberType("react");
				lastPostStandardShare.setNumberType("share");
				lastPostStandardComment.setNumberType("comment");

				lastPostStandardReact.setType("increasePost");
				lastPostStandardShare.setType("increasePost");
				lastPostStandardComment.setType("increasePost");

				lastStandardService.save(lastPostStandardReact);
				lastStandardService.save(lastPostStandardShare);
				lastStandardService.save(lastPostStandardComment);
			}
		}
	}

	private static double calculateSD(double numArray[]) {
		double sum = 0.0, standardDeviation = 0.0;
		int length = numArray.length;

		for (double num : numArray) {
			sum += num;
		}

		double mean = sum / length;

		for (double num : numArray) {
			standardDeviation += Math.pow(num - mean, 2);
		}

		return Math.sqrt(standardDeviation / length);
	}

	private static double mean(double[] m) {
		double sum = 0;
		for (int i = 0; i < m.length; i++) {
			sum += m[i];
		}
		return sum / m.length;
	}

	private void DetectCrisisInCurrent(String keyword, TextAPIClient client, List<Crisis> listCrisis) throws Exception {
		List<Post> listPost = getRecentPost(keyword);
		if (listPost.size() > 0) {
			CheckMeaningCurrentPostThread CheckMeaningCurrentPostThread = new CheckMeaningCurrentPostThread(client,
					keyword, listPost, crisisService, commentService, postService, keywordService, notificationService,
					notificationContentService, userInfoService, userService, listCrisis, notificationTokenService,
					negativeRatioService, lastStandardService);
			CheckMeaningCurrentPostThread.start();
		}
	}

	private List<Post> getRecentPost(String keyword) {
		// Get The list of post with latest date in DB
		List<Post> posts = postService.getEachPostContentWithLatestDate(keyword);
		List<Post> returnList = new ArrayList<>();
		for (int i = 0; i < posts.size(); i++) {
			Post post = posts.get(i);
			long millis = System.currentTimeMillis();
			Date date = new Date(millis);
			long diffInMillies = Math.abs(date.getTime() - post.getCrawlDate().getTime());
			long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			if (diff < diffenrentDate) {
				returnList.add(post);
			}
		}
		return returnList;
	}

	private List<Post> getNewPost(String keyword) {
		// Get The list of post with latest date in DB
		List<Post> posts = postService.getNewPost(keyword, true);
		return posts;
	}
}

class CheckMeaningCurrentPostThread extends BaseThread {

	public CheckMeaningCurrentPostThread(TextAPIClient client, String keyword, List<Post> listPost,
			CrisisService crisisService, CommentService commentService, PostService postService,
			KeywordService keywordService, NotificationService notificationService,
			NotificationContentService notificationContentService, UserInfoService userInfoService,
			UserService userService, List<Crisis> listCrisis, NotificationTokenService notificationTokenService,
			NegativeRatioService negativeRatioService, LastStandardService lastStandardService) {
		this.client = client;
		this.keyword = keyword;
		this.listPost = listPost;
		this.crisisService = crisisService;
		this.commentService = commentService;
		this.postService = postService;
		this.keywordService = keywordService;
		this.notificationService = notificationService;
		this.notificationContentService = notificationContentService;
		this.userInfoService = userInfoService;
		this.userService = userService;
		this.listCrisis = listCrisis;
		this.notificationTokenService = notificationTokenService;
		this.negativeRatioService = negativeRatioService;
		this.lastStandardService = lastStandardService;
	}

	@Override
	public synchronized void start() {
		EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
		List<Post> listPostNegative = new ArrayList<>();

		LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "post", "react");
		LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "post", "share");
		LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "post", "comment");

		double reactStandard = lastPostStandardReact.getLastStandard();
		double reactMean = lastPostStandardReact.getLastMean();
		double react_anomaly_cut_off = reactStandard * 2;
		double react_upper_limit = reactMean + react_anomaly_cut_off;

		double shareStandard = lastPostStandardShare.getLastStandard();
		double shareMean = lastPostStandardShare.getLastMean();
		double share_anomaly_cut_off = shareStandard * 2;
		double share_upper_limit = shareMean + share_anomaly_cut_off;

		double commentStandard = lastPostStandardComment.getLastStandard();
		double commentMean = lastPostStandardComment.getLastMean();
		double comment_anomaly_cut_off = commentStandard * 2;
		double comment_upper_limit = commentMean + comment_anomaly_cut_off;
		try {
			if (listPost.size() == 0) {
				if (listCrisis.size() > 0) {
					NotificationController notiController = new NotificationController();
					notiController.sendNotification(listCrisis, keyword, postService, commentService,
							notificationService, notificationContentService, userInfoService, crisisService,
							userService, keywordService, notificationTokenService);
				}
				this.interrupt();
			}
			for (int i = 0; i < listPost.size(); i++) {
				if (totalCount - countHit < entity_sentiment_count) {
					countHit = 0;
					this.sleep(1000 * 60 * 1);
				}
				Post post = listPost.get(i);
				// builder.setText(post.getPostContent());
				// EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
				// List<EntitiySentiments> list = elsa.getEntitiySentiments();
				// countHit += entity_sentiment_count;
				// if (list.size() > 0) {
				// for (int x = 0; x < list.size(); x++) {
				// EntitiySentiments sen = list.get(x);
				// String word = sen.getMentions()[0].getText();
				// String mean = sen.getOverallSentiment().getPolarity();
				// float confidence = sen.getOverallSentiment().getConfidence();
				// if (mean.equals(negative) && confidence > lowerConfidence
				// && word.toLowerCase().equals(keyword.toLowerCase())) {
				// listPostNegative.add(post);
				if (post.getNumberOfReply() > comment_upper_limit || post.getNumberOfReweet() > share_upper_limit
						|| post.getNumberOfReact() > react_upper_limit) {
					// Save crisis and check if already add or not
					insertPostCrisis(post, crisisService);
				}
			}
			// }
			// }
			// }
			// double negativeRatio = listPost.size() / listPostNegative.size();
			// NegativeRatio lastNegativeRatio =
			// negativeRatioService.getNegativeRatio(keyword, "post");
			// long millis = System.currentTimeMillis();
			// Date date = new Date(millis);
			// boolean isNegativeIncrease = false;
			// if (lastNegativeRatio != null) {
			// if (lastNegativeRatio.getUpdateDate().before(date)) {
			// long diffInMillies = Math.abs(date.getTime() -
			// lastNegativeRatio.getUpdateDate().getTime());
			// long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			// if (diff > differenceHour) {
			// if (lastNegativeRatio.getRatio() < negativeRatio) {
			// if (negativeRatio - lastNegativeRatio.getRatio() < ratioLimit) {
			// lastNegativeRatio.setRatio(negativeRatio);
			// lastNegativeRatio.setUpdateDate(date);
			// negativeRatioService.save(lastNegativeRatio);
			// isNegativeIncrease = true;
			// } else {
			// lastNegativeRatio.setRatio(negativeRatio);
			// lastNegativeRatio.setUpdateDate(date);
			// negativeRatioService.save(lastNegativeRatio);
			// }
			// } else {
			// lastNegativeRatio.setRatio(negativeRatio);
			// lastNegativeRatio.setUpdateDate(date);
			// negativeRatioService.save(lastNegativeRatio);
			// }
			// }
			// }
			// } else {
			// lastNegativeRatio = new NegativeRatio();
			// lastNegativeRatio.setKeyword(keyword);
			// lastNegativeRatio.setType("post");
			// lastNegativeRatio.setUpdateDate(date);
			// lastNegativeRatio.setRatio(negativeRatio);
			// negativeRatioService.save(lastNegativeRatio);
			// }
			// if (isNegativeIncrease) {
			// NotificationController notiController = new NotificationController();
			// notiController.sendListPostNotification(listPost, keyword, postService,
			// commentService,
			// notificationService, notificationContentService, userInfoService,
			// crisisService, userService,
			// keywordService, notificationTokenService);
			// }
			Thread.sleep(1000 * 60 * 1);
			List<Comment> listComment = new ArrayList<>();
			for (int i = 0; i < listPost.size(); i++) {
				Post post = listPost.get(i);
				listComment.addAll(getRecentComment(post.getId()));
			}
			CheckMeaningCurrentCommentThread CheckMeaningCurrentCommentThread = new CheckMeaningCurrentCommentThread(
					client, keyword, listComment, crisisService, postService, commentService, keywordService,
					notificationService, notificationContentService, userInfoService, userService, listCrisis,
					notificationTokenService, lastStandardService);
			CheckMeaningCurrentCommentThread.start();
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertPostCrisis(Post post, CrisisService crisisService) {
		Crisis result = crisisService.findCrisis(post.getPostId(), "post", keyword);
		if (result == null) {
			Crisis crisis = new Crisis();
			crisis.setContentId(post.getPostId());
			crisis.setType(type);
			crisis.setKeyword(keyword);
			if (!containCrisis(listCrisis, crisis)) {
				listCrisis.add(crisis);
			}
			crisisService.saveCrisis(crisis);
			// send crisis to notification
		} else {
			if (!containCrisis(listCrisis, result)) {
				listCrisis.add(result);
			}
		}
	}

	private boolean containCrisis(List<Crisis> listCrisis, Crisis crisis) {
		boolean result = false;
		for (int i = 0; i < listCrisis.size(); i++) {
			Crisis crisisInList = listCrisis.get(i);
			if (crisisInList.getContentId() == crisis.getContentId()
					&& crisisInList.getKeyword().equals(crisis.getKeyword())
					&& crisisInList.getType().equals(crisis.getType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	private List<Comment> getRecentComment(String PostId) {
		// Get The list of comment with latest date in DB
		List<Comment> listComment = commentService.getCommentByPostId(PostId);
		return listComment;
	}
}

class CheckMeaningCurrentCommentThread extends BaseThread {
	public CheckMeaningCurrentCommentThread(TextAPIClient client, String keyword, List<Comment> listComment,
			CrisisService crisisService, PostService postService, CommentService commentService,
			KeywordService keywordService, NotificationService notificationService,
			NotificationContentService notificationContentService, UserInfoService userInfoService,
			UserService userService, List<Crisis> listCrisis, NotificationTokenService notificationTokenService,
			LastStandardService lastStandardService) {
		this.client = client;
		this.keyword = keyword;
		this.listComment = listComment;
		this.crisisService = crisisService;
		this.postService = postService;
		this.commentService = commentService;
		this.keywordService = keywordService;
		this.notificationService = notificationService;
		this.notificationContentService = notificationContentService;
		this.userInfoService = userInfoService;
		this.userService = userService;
		this.listCrisis = listCrisis;
		this.notificationTokenService = notificationTokenService;
		this.lastStandardService = lastStandardService;
	}

	@Override
	public synchronized void start() {
		EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
		List<Comment> listCommentNegative = new ArrayList<>();
		LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "comment", "react");
		LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "comment", "comment");

		double reactStandard = lastCommentStandardReact.getLastStandard();
		double reactMean = lastCommentStandardReact.getLastMean();
		double react_anomaly_cut_off = reactStandard * 2;
		double react_upper_limit = reactMean + react_anomaly_cut_off;

		double commentStandard = lastCommentStandardComment.getLastStandard();
		double commentMean = lastCommentStandardComment.getLastMean();
		double comment_anomaly_cut_off = commentStandard * 2;
		double comment_upper_limit = commentMean + comment_anomaly_cut_off;
		try {
			for (int i = 0; i < listComment.size(); i++) {
				if (totalCount - countHit < entity_sentiment_count) {
					countHit = 0;
					this.sleep(1000 * 60 * 1);
				}
				Comment comment = listComment.get(i);
				// builder.setText(comment.getCommentContent());
				// EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
				// List<EntitiySentiments> list = elsa.getEntitiySentiments();
				// countHit += entity_sentiment_count;
				// if (list.size() > 0) {
				// for (int x = 0; x < list.size(); x++) {
				// EntitiySentiments sen = list.get(x);
				// String word = sen.getMentions()[0].getText();
				// String mean = sen.getOverallSentiment().getPolarity();
				// float confidence = sen.getOverallSentiment().getConfidence();
				// if (mean.equals(negative) && confidence > lowerConfidence
				// && word.toLowerCase().equals(keyword.toLowerCase())) {
				if (comment.getNumberOfReply() > comment_upper_limit
						|| comment.getNumberOfReact() > react_upper_limit) {
					insertCommentCrisis(comment, crisisService);
				}
			}
			// }
			// } else {
			// if (totalCount - countHit < sentiment_count) {
			// countHit = 0;
			// this.sleep(1000 * 60 * 1);
			// }
			// SentimentParams.Builder sentimentBuilder = SentimentParams.newBuilder();
			// sentimentBuilder.setText(comment.getCommentContent());
			// sentimentBuilder.setMode("tweet");
			// Sentiment sentiment = client.sentiment(sentimentBuilder.build());
			// countHit += sentiment_count;
			// if (sentiment.getPolarity().equals(negative)
			// && sentiment.getPolarityConfidence() > lowerConfidence) {
			// if (comment.getNumberOfReply() > comment_upper_limit
			// || comment.getNumberOfReact() > react_upper_limit) {
			// insertCommentCrisis(comment, crisisService);
			// }
			// }
			// }
			// }
			// double negativeRatio = listComment.size() / listCommentNegative.size();
			// NegativeRatio lastNegativeRatio =
			// negativeRatioService.getNegativeRatio(keyword, "comment");
			// long millis = System.currentTimeMillis();
			// Date date = new Date(millis);
			// boolean isNegativeIncrease = false;
			// if (lastNegativeRatio != null) {
			// if (lastNegativeRatio.getUpdateDate().before(date)) {
			// long diffInMillies = Math.abs(date.getTime() -
			// lastNegativeRatio.getUpdateDate().getTime());
			// long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			// if (diff > differenceHour) {
			// if (lastNegativeRatio.getRatio() < negativeRatio) {
			// if (negativeRatio - lastNegativeRatio.getRatio() < ratioLimit) {
			// lastNegativeRatio.setRatio(negativeRatio);
			// lastNegativeRatio.setUpdateDate(date);
			// negativeRatioService.save(lastNegativeRatio);
			// isNegativeIncrease = true;
			// } else {
			// lastNegativeRatio.setRatio(negativeRatio);
			// lastNegativeRatio.setUpdateDate(date);
			// negativeRatioService.save(lastNegativeRatio);
			// }
			// } else {
			// lastNegativeRatio.setRatio(negativeRatio);
			// lastNegativeRatio.setUpdateDate(date);
			// negativeRatioService.save(lastNegativeRatio);
			// }
			// }
			// }
			// } else {
			// lastNegativeRatio = new NegativeRatio();
			// lastNegativeRatio.setKeyword(keyword);
			// lastNegativeRatio.setType("comment");
			// lastNegativeRatio.setUpdateDate(date);
			// lastNegativeRatio.setRatio(negativeRatio);
			// negativeRatioService.save(lastNegativeRatio);
			// }
			// if (isNegativeIncrease) {
			// NotificationController notiController = new NotificationController();
			// notiController.sendListCommentNotification(listCommentNegative, keyword,
			// postService, commentService,
			// notificationService, notificationContentService, userInfoService,
			// crisisService, userService,
			// keywordService, notificationTokenService);
			// }
			this.sleep(1000 * 60 * 1);
			List<Post> listPost = getIncreasePost(keyword);
			CheckMeaningIncreasePostThread CheckMeaningIncreasePostThread = new CheckMeaningIncreasePostThread(client,
					keyword, listPost, crisisService, commentService, keywordService, notificationService,
					notificationContentService, userInfoService, userService, postService, listCrisis,
					notificationTokenService, lastStandardService);
			CheckMeaningIncreasePostThread.start();
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertCommentCrisis(Comment comment, CrisisService crisisService) {
		Crisis result = crisisService.findCrisis(comment.getCommentId(), "comment", keyword);
		if (result == null) {
			Crisis crisis = new Crisis();
			crisis.setContentId(comment.getCommentId());
			crisis.setType(type);
			crisis.setKeyword(keyword);
			if (!containCrisis(listCrisis, crisis)) {
				listCrisis.add(crisis);
			}
			crisisService.saveCrisis(crisis);
		} else {
			if (!containCrisis(listCrisis, result)) {
				listCrisis.add(result);
			}
		}
	}

	private boolean containCrisis(List<Crisis> listCrisis, Crisis crisis) {
		boolean result = false;
		for (int i = 0; i < listCrisis.size(); i++) {
			Crisis crisisInList = listCrisis.get(i);
			if (crisisInList.getContentId() == crisis.getContentId()
					&& crisisInList.getKeyword().equals(crisis.getKeyword())
					&& crisisInList.getType().equals(crisis.getType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	private List<Post> getIncreasePost(String keyword) {
		// Get the list of post with two latest date in DB
		List<Post> listPost = postService.getPostContentWithTwoLatestDate(keyword);
		List<Post> resultList = new ArrayList<>();
		List<Post> sameContentPost = new ArrayList<>();
		List<Post> sameContentPostSorted = new ArrayList<>();
		for (int i = 0; i < listPost.size(); i++) {
			if (i < listPost.size() - 1) {
				Post post = listPost.get(i);
				sameContentPost = getListSameContent(listPost, post);
				sameContentPostSorted = sortByCrawlDate(sameContentPost);
				if (!checkExist(resultList, sameContentPostSorted.get(0).getPostContent())
						&& sameContentPostSorted.size() == 2) {
					resultList.addAll(sameContentPostSorted);
				}
			}
		}
		return resultList;
	}

	private boolean checkExist(List<Post> listPost, String postContent) {
		for (int i = 0; i < listPost.size(); i++) {
			String content = listPost.get(i).getPostContent();
			if (postContent.equals(content)) {
				return true;
			}
		}
		return false;
	}

	private List<Post> getListSameContent(List<Post> listPost, Post checkPost) {
		List<Post> result = new ArrayList<>();
		for (int i = 0; i < listPost.size(); i++) {
			Post post = listPost.get(i);
			if (checkPost.getPostContent().equals(post.getPostContent())) {
				result.add(post);
			}
		}
		return result;
	}

	private List<Post> sortByCrawlDate(List<Post> listPost) {
		List<Post> result = new ArrayList<>();
		for (int i = 0; i < listPost.size(); i++) {
			Post post = listPost.get(i);
			if (result.size() == 0) {
				result.add(post);
			} else if (result.size() == 1) {
				if (post.getCrawlDate().before(result.get(0).getCrawlDate())) {
					Post newPost = result.get(0);
					result.set(0, post);
					result.add(newPost);
				} else if (post.getCrawlDate().after(result.get(0).getCrawlDate())) {
					result.add(post);
				}
			} else if (result.size() == 2) {
				if (post.getCrawlDate().after(result.get(1).getCrawlDate())) {
					result.set(0, result.get(1));
					result.set(1, post);
				} else if (post.getCrawlDate().after(result.get(0).getCrawlDate())
						&& post.getCrawlDate().before(result.get(1).getCrawlDate())) {
					result.set(0, post);
				}
			}
		}
		return result;
	}

}

class CheckMeaningIncreasePostThread extends BaseThread {

	public CheckMeaningIncreasePostThread(TextAPIClient client, String keyword, List<Post> listPost,
			CrisisService crisisService, CommentService commentService, KeywordService keywordService,
			NotificationService notificationService, NotificationContentService notificationContentService,
			UserInfoService userInfoService, UserService userService, PostService postService, List<Crisis> listCrisis,
			NotificationTokenService notificationTokenService, LastStandardService lastStandardService) {
		this.client = client;
		this.keyword = keyword;
		this.listPost = listPost;
		this.crisisService = crisisService;
		this.commentService = commentService;
		this.keywordService = keywordService;
		this.notificationService = notificationService;
		this.notificationContentService = notificationContentService;
		this.userInfoService = userInfoService;
		this.userService = userService;
		this.postService = postService;
		this.listCrisis = listCrisis;
		this.notificationTokenService = notificationTokenService;
		this.lastStandardService = lastStandardService;
	}

	private boolean containCrisis(List<Crisis> listCrisis, Crisis crisis) {
		boolean result = false;
		for (int i = 0; i < listCrisis.size(); i++) {
			Crisis crisisInList = listCrisis.get(i);
			if (crisisInList.getContentId() == crisis.getContentId()
					&& crisisInList.getKeyword().equals(crisis.getKeyword())
					&& crisisInList.getType().equals(crisis.getType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void insertPostCrisis(Post post, CrisisService crisisService) {
		Crisis result = crisisService.findCrisis(post.getPostId(), "post", keyword);
		if (result == null) {
			Crisis crisis = new Crisis();
			crisis.setContentId(post.getPostId());
			crisis.setType(type);
			crisis.setKeyword(keyword);
			if (!containCrisis(listCrisis, crisis)) {
				listCrisis.add(crisis);
			}
			crisisService.saveCrisis(crisis);
			// send crisis to notification
		} else {
			if (!containCrisis(listCrisis, result)) {
				listCrisis.add(result);
			}
		}
	}

	@Override
	public synchronized void start() {
		EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
		LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "increasePost", "react");
		LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "increasePost", "share");
		LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "increasePost", "comment");
		if (lastPostStandardReact != null && lastPostStandardShare != null && lastPostStandardComment != null) {
			double react_upper_limit = 0, share_upper_limit = 0, comment_upper_limit = 0;
			if (lastPostStandardReact != null) {
				double reactStandard = lastPostStandardReact.getLastStandard();
				double reactMean = lastPostStandardReact.getLastMean();
				double react_anomaly_cut_off = reactStandard * 2;
				react_upper_limit = reactMean + react_anomaly_cut_off;
			}
			if (lastPostStandardShare != null) {
				double shareStandard = lastPostStandardShare.getLastStandard();
				double shareMean = lastPostStandardShare.getLastMean();
				double share_anomaly_cut_off = shareStandard * 2;
				share_upper_limit = shareMean + share_anomaly_cut_off;
			}
			if (lastPostStandardComment != null) {
				double commentStandard = lastPostStandardComment.getLastStandard();
				double commentMean = lastPostStandardComment.getLastMean();
				double comment_anomaly_cut_off = commentStandard * 2;
				comment_upper_limit = commentMean + comment_anomaly_cut_off;
			}
			try {
				if (listPost.size() < 2) {
					if (listCrisis.size() > 0) {
						NotificationController notiController = new NotificationController();
						notiController.sendNotification(listCrisis, keyword, postService, commentService,
								notificationService, notificationContentService, userInfoService, crisisService,
								userService, keywordService, notificationTokenService);
					}
					this.interrupt();
				}
				for (int i = 0; i < listPost.size(); i = i + 2) {
					if (totalCount - countHit < entity_sentiment_count) {
						countHit = 0;
						this.sleep(1000 * 60 * 1);
					}
					Post post = listPost.get(i);
					Post nextPost = listPost.get(i + 1);
					// builder.setText(post.getPostContent());
					// EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
					// List<EntitiySentiments> list = elsa.getEntitiySentiments();
					// countHit += entity_sentiment_count;
					// if (list.size() > 0) {
					// for (int x = 0; x < list.size(); x++) {
					// EntitiySentiments sen = list.get(x);
					// String mean = sen.getOverallSentiment().getPolarity();
					// float confidence = sen.getOverallSentiment().getConfidence();
					// String word = sen.getMentions()[0].getText();
					// if (mean.equals(negative) && confidence > lowerConfidence
					// && word.toLowerCase().equals(keyword.toLowerCase())) {
					if ((post.getNumberOfReply() - nextPost.getNumberOfReply()) > comment_upper_limit
							|| (post.getNumberOfReweet() - nextPost.getNumberOfReweet()) > share_upper_limit
							|| (post.getNumberOfReact() - nextPost.getNumberOfReact()) > react_upper_limit) {
						// Add Crisis To Db
						insertPostCrisis(nextPost, crisisService);
					}
				}
				// }
				// }
				// }
				this.sleep(100 * 60 * 1);
				List<Comment> lastPostComment = new ArrayList<>();
				List<Comment> newPostComment = new ArrayList<>();
				List<Comment> listComment = new ArrayList<>();
				for (int i = 0; i < listPost.size(); i = i + 2) {
					Post post = listPost.get(i);
					Post nextPost = listPost.get(i + 1);
					lastPostComment.addAll(getIncreaseComment(post.getId()));
					newPostComment.addAll(getIncreaseComment(nextPost.getId()));
				}
				for (int i = 0; i < lastPostComment.size(); i++) {
					Comment lastComment = lastPostComment.get(i);
					int result = findComment(newPostComment, lastComment);
					if (result != -1) {
						listComment.add(lastComment);
						listComment.add(newPostComment.get(result));
					}
				}
				CheckMeaningIncreaseCommentThread CheckMeaningIncreaseCommentThread = new CheckMeaningIncreaseCommentThread(
						client, keyword, listComment, crisisService, keywordService, notificationService,
						notificationContentService, userInfoService, userService, commentService, postService,
						listCrisis, lastStandardService);
				CheckMeaningIncreaseCommentThread.start();
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int findComment(List<Comment> listComment, Comment comment) {
		for (int i = 0; i < listComment.size(); i++) {
			if (comment.getCommentContent().equals(listComment.get(i).getCommentContent())) {
				return i;
			}
		}
		return -1;
	}

	private List<Comment> getIncreaseComment(String PostId) {
		// Get the list of comment with two latest date in DB
		List<Comment> listComment = commentService.getCommentByPostId(PostId);
		return listComment;
	}
}

class CheckMeaningIncreaseCommentThread extends BaseThread {

	public CheckMeaningIncreaseCommentThread(TextAPIClient client, String keyword, List<Comment> listComment,
			CrisisService crisisService, KeywordService keywordService, NotificationService notificationService,
			NotificationContentService notificationContentService, UserInfoService userInfoService,
			UserService userService, CommentService commentService, PostService postService, List<Crisis> listCrisis,
			LastStandardService lastStandardService) {
		this.client = client;
		this.keyword = keyword;
		this.listComment = listComment;
		this.crisisService = crisisService;
		this.keywordService = keywordService;
		this.notificationService = notificationService;
		this.notificationContentService = notificationContentService;
		this.userInfoService = userInfoService;
		this.userService = userService;
		this.commentService = commentService;
		this.postService = postService;
		this.listCrisis = listCrisis;
		this.lastStandardService = lastStandardService;
	}

	private boolean containCrisis(List<Crisis> listCrisis, Crisis crisis) {
		boolean result = false;
		for (int i = 0; i < listCrisis.size(); i++) {
			Crisis crisisInList = listCrisis.get(i);
			if (crisisInList.getContentId() == crisis.getContentId()
					&& crisisInList.getKeyword().equals(crisis.getKeyword())
					&& crisisInList.getType().equals(crisis.getType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void insertCommentCrisis(Comment comment, CrisisService crisisService) {
		Crisis result = crisisService.findCrisis(comment.getCommentId(), "comment", keyword);
		if (result == null) {
			Crisis crisis = new Crisis();
			crisis.setContentId(comment.getCommentId());
			crisis.setType(type);
			crisis.setKeyword(keyword);
			if (!containCrisis(listCrisis, crisis)) {
				listCrisis.add(crisis);
			}
			crisisService.saveCrisis(crisis);
		} else {
			if (!containCrisis(listCrisis, result)) {
				listCrisis.add(result);
			}
		}
	}

	@Override
	public synchronized void start() {
		EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
		LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "increaseComment",
				"react");
		LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "increaseComment",
				"comment");
		if (lastCommentStandardReact != null && lastCommentStandardComment != null) {
			int size = 0;
			double reactArray[] = new double[listComment.size() / 2];
			double commentArray[] = new double[listComment.size() / 2];
			double react_upper_limit = 0, comment_upper_limit = 0;
			for (int i = 0; i < listComment.size(); i = i + 2) {
				Comment lastComment = listComment.get(i);
				Comment newComment = listComment.get(i + 1);
				reactArray[i] = newComment.getNumberOfReact() - lastComment.getNumberOfReact();
				commentArray[i] = newComment.getNumberOfReply() - lastComment.getNumberOfReply();
			}
			if (lastCommentStandardReact != null) {
				double reactStandard = lastCommentStandardReact.getLastStandard();
				double reactMean = lastCommentStandardReact.getLastMean();
				double react_anomaly_cut_off = reactStandard * 2;
				react_upper_limit = reactMean + react_anomaly_cut_off;
			}
			if (lastCommentStandardComment != null) {
				double commentStandard = lastCommentStandardComment.getLastStandard();
				double commentMean = lastCommentStandardComment.getLastMean();
				double comment_anomaly_cut_off = commentStandard * 2;
				comment_upper_limit = commentMean + comment_anomaly_cut_off;
			}
			try {
				for (int i = 0; i < listComment.size(); i += 2) {
					if (totalCount - countHit < entity_sentiment_count) {
						countHit = 0;
						this.sleep(1000 * 60 * 1);
					}
					Comment lastComment = listComment.get(i);
					Comment newComment = listComment.get(i + 1);
					// builder.setText(newComment.getCommentContent());
					// EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
					// List<EntitiySentiments> list = elsa.getEntitiySentiments();
					// countHit += entity_sentiment_count;
					// if (list.size() > 0) {
					// for (int x = 0; x < list.size(); x++) {
					// EntitiySentiments sen = list.get(x);
					// String mean = sen.getOverallSentiment().getPolarity();
					// float confidence = sen.getOverallSentiment().getConfidence();
					// String word = sen.getMentions()[0].getText();
					// if (mean.equals(negative) && confidence > lowerConfidence
					// && word.toLowerCase().equals(keyword.toLowerCase())) {
					if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) > comment_upper_limit
							|| (lastComment.getNumberOfReact() - newComment.getNumberOfReact()) > react_upper_limit) {
						// Add Crisis to Db
						insertCommentCrisis(newComment, crisisService);
					}
				}
				// }
				// } else {
				// SentimentParams.Builder sentimentBuilder = SentimentParams.newBuilder();
				// sentimentBuilder.setText(newComment.getCommentContent());
				// sentimentBuilder.setMode("tweet");
				// Sentiment sentiment = client.sentiment(sentimentBuilder.build());
				// countHit += sentiment_count;
				// if (sentiment.getPolarity().equals(negative)
				// && sentiment.getPolarityConfidence() > lowerConfidence) {
				// if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) >
				// comment_upper_limit
				// || (lastComment.getNumberOfReact()
				// - newComment.getNumberOfReact()) > react_upper_limit) {
				// // Add Crisis to Db
				// insertCommentCrisis(newComment, crisisService);
				// }
				// }
				// }
				// }
				this.sleep(1000 * 60 * 1);
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
