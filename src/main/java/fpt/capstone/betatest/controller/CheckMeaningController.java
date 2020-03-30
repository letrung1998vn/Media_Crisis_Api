
package fpt.capstone.betatest.controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.EntityLevelSentimentParams;
import com.aylien.textapi.parameters.SentimentParams;
import com.aylien.textapi.responses.EntitiesSentiment;
import com.aylien.textapi.responses.EntitiySentiments;
import com.aylien.textapi.responses.Sentiment;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.Keyword_Crawler;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.BaseThread;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.KeywordCrawlerService;
import fpt.capstone.betatest.services.KeywordService;
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

	@GetMapping("check")
	public void checkMeaning() throws Exception {
		TextAPIClient client = new TextAPIClient("43faa103", "f2aaee05b21dabe934b89bd3198801e8");
		CheckThread check = new CheckThread(client, crisisService, commentService, postService, keywordCrawlerService,
				keywordService, notificationService, notificationContentService, userInfoService, userService,
				notificationTokenService);
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

	public CheckThread(TextAPIClient client, CrisisService crisisService, CommentService commentService,
			PostService postService, KeywordCrawlerService keywordCrawlerService, KeywordService keywordService,
			NotificationService notificationService, NotificationContentService notificationContentService,
			UserInfoService userInfoService, UserService userService,
			NotificationTokenService notificationTokenService) {
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
	}

	@Override
	public synchronized void start() {
		// Get all Keyword from keyword crawler
		List<Keyword_Crawler> listKeyword = keywordCrawlerService.getAllKeyword();
		List<Crisis> listCrisis = new ArrayList<>();
		for (int i = 0; i < listKeyword.size(); i++) {
			try {
				System.out.println("Keyword: " + listKeyword.get(i).getKeyword());
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

	private void DetectCrisisInCurrent(String keyword, TextAPIClient client, List<Crisis> listCrisis) throws Exception {
		List<Post> listPost = getRecentPost(keyword);
		CheckMeaningCurrentPostThread CheckMeaningCurrentPostThread = new CheckMeaningCurrentPostThread(client, keyword,
				listPost, crisisService, commentService, postService, keywordService, notificationService,
				notificationContentService, userInfoService, userService, listCrisis, notificationTokenService);
		CheckMeaningCurrentPostThread.start();
	}

	private List<Post> getRecentPost(String keyword) {
		// Get The list of post with latest date in DB
		List<Post> posts = postService.getEachPostContentWithLatestDate(keyword);
		return posts;
	}
}

class CheckMeaningCurrentPostThread extends BaseThread {

	public CheckMeaningCurrentPostThread(TextAPIClient client, String keyword, List<Post> listPost,
			CrisisService crisisService, CommentService commentService, PostService postService,
			KeywordService keywordService, NotificationService notificationService,
			NotificationContentService notificationContentService, UserInfoService userInfoService,
			UserService userService, List<Crisis> listCrisis, NotificationTokenService notificationTokenService) {
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
	}

	@Override
	public synchronized void start() {
		EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
		double reactArray[] = new double[listPost.size()];
		double shareArray[] = new double[listPost.size()];
		double commentArray[] = new double[listPost.size()];
		for (int i = 0; i < listPost.size(); i++) {
			Post post = listPost.get(i);
			reactArray[i] = post.getNumberOfReact();
			shareArray[i] = post.getNumberOfReweet();
			commentArray[i] = post.getNumberOfReply();
		}
		double reactStandart = calculateSD(reactArray);
		double reactMean = mean(reactArray);
		double react_anomaly_cut_off = reactStandart * 2;
		double react_lower_limit = reactMean - react_anomaly_cut_off;
		double react_upper_limit = reactMean + react_anomaly_cut_off;

		double shareStandart = calculateSD(shareArray);
		double shareMean = mean(shareArray);
		double share_anomaly_cut_off = shareStandart * 2;
		double share_lower_limit = shareMean - share_anomaly_cut_off;
		double share_upper_limit = shareMean + share_anomaly_cut_off;

		double commentStandart = calculateSD(commentArray);
		double commentMean = mean(commentArray);
		double comment_anomaly_cut_off = commentStandart * 2;
		double comment_lower_limit = commentMean - comment_anomaly_cut_off;
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
				builder.setText(post.getPostContent());
				EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
				List<EntitiySentiments> list = elsa.getEntitiySentiments();
				countHit += entity_sentiment_count;
				if (list.size() > 0) {
					for (int x = 0; x < list.size(); x++) {
						EntitiySentiments sen = list.get(x);
						String word = sen.getMentions()[0].getText();
						String mean = sen.getOverallSentiment().getPolarity();
						float confidence = sen.getOverallSentiment().getConfidence();
						if (mean.equals(negative) && confidence > lowerConfidence
								&& word.toLowerCase().equals(keyword.toLowerCase())) {
							if (reactMean < lowMean && shareMean < lowMean && commentMean < lowMean) {
								if (post.getNumberOfReply() > comment_upper_limit
										|| post.getNumberOfReweet() > share_upper_limit
										|| post.getNumberOfReact() > react_upper_limit) {
									// Save crisis and check if already add or not
									insertPostCrisis(post, crisisService);
								}
							} else {
								if (reactStandart < lowStandard || shareStandart < lowStandard
										|| commentStandart < lowStandard) {
									if (post.getNumberOfReply() > commentMean || post.getNumberOfReweet() > shareMean
											|| post.getNumberOfReact() > reactMean) {
										// Save crisis and check if already add or not
										insertPostCrisis(post, crisisService);
									}
								} else {
									insertPostCrisis(post, crisisService);
								}

							}
						}
					}
				}
			}
			Thread.sleep(1000 * 60 * 1);
			List<Comment> listComment = new ArrayList<>();
			for (int i = 0; i < listPost.size(); i++) {
				Post post = listPost.get(i);
				listComment.addAll(getRecentComment(post.getId()));
			}
			CheckMeaningCurrentCommentThread CheckMeaningCurrentCommentThread = new CheckMeaningCurrentCommentThread(
					client, keyword, listComment, crisisService, postService, commentService, keywordService,
					notificationService, notificationContentService, userInfoService, userService, listCrisis,
					notificationTokenService);
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
			UserService userService, List<Crisis> listCrisis, NotificationTokenService notificationTokenService) {
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
	}

	@Override
	public synchronized void start() {
		EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
		double reactArray[] = new double[listComment.size()];
		double commentArray[] = new double[listComment.size()];
		for (int i = 0; i < listComment.size(); i++) {
			Comment comment = listComment.get(i);
			reactArray[i] = comment.getNumberOfReact();
			commentArray[i] = comment.getNumberOfReply();
		}
		double reactStandart = calculateSD(reactArray);
		double reactMean = mean(reactArray);
		double react_anomaly_cut_off = reactStandart * 2;
		double react_lower_limit = reactMean - react_anomaly_cut_off;
		double react_upper_limit = reactMean + react_anomaly_cut_off;

		double commentStandart = calculateSD(commentArray);
		double commentMean = mean(commentArray);
		double comment_anomaly_cut_off = commentStandart * 2;
		double comment_lower_limit = commentMean - comment_anomaly_cut_off;
		double comment_upper_limit = commentMean + comment_anomaly_cut_off;
		try {
			for (int i = 0; i < listComment.size(); i++) {
				if (totalCount - countHit < entity_sentiment_count) {
					countHit = 0;
					this.sleep(1000 * 60 * 1);
				}
				Comment comment = listComment.get(i);
				builder.setText(comment.getCommentContent());
				EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
				List<EntitiySentiments> list = elsa.getEntitiySentiments();
				countHit += entity_sentiment_count;
				if (list.size() > 0) {
					for (int x = 0; x < list.size(); x++) {
						EntitiySentiments sen = list.get(x);
						String word = sen.getMentions()[0].getText();
						String mean = sen.getOverallSentiment().getPolarity();
						float confidence = sen.getOverallSentiment().getConfidence();
						if (mean.equals(negative) && confidence > lowerConfidence
								&& word.toLowerCase().equals(keyword.toLowerCase())) {
							if (reactMean < lowMean && commentMean < lowMean) {
								insertCommentCrisis(comment, crisisService);
							} else {
								if (reactStandart < lowStandard || commentStandart < lowStandard) {
									if (comment.getNumberOfReply() > commentMean
											|| comment.getNumberOfReact() > reactMean) {
										insertCommentCrisis(comment, crisisService);
									}
								} else {
									insertCommentCrisis(comment, crisisService);
								}
							}
						}
					}
				} else {
					if (totalCount - countHit < sentiment_count) {
						countHit = 0;
						this.sleep(1000 * 60 * 1);
					}
					SentimentParams.Builder sentimentBuilder = SentimentParams.newBuilder();
					sentimentBuilder.setText(comment.getCommentContent());
					sentimentBuilder.setMode("tweet");
					Sentiment sentiment = client.sentiment(sentimentBuilder.build());
					countHit += sentiment_count;
					if (sentiment.getPolarity().equals(negative)
							&& sentiment.getPolarityConfidence() > lowerConfidence) {
						if (reactMean < lowMean && commentMean < lowMean) {
							if (comment.getNumberOfReply() > comment_upper_limit
									|| comment.getNumberOfReact() > react_upper_limit) {
								insertCommentCrisis(comment, crisisService);
							}
						} else {
							if (reactStandart < lowStandard || commentStandart < lowStandard) {
								if (comment.getNumberOfReply() > commentMean
										|| comment.getNumberOfReact() > reactMean) {
									insertCommentCrisis(comment, crisisService);
								}
							} else {
								insertCommentCrisis(comment, crisisService);
							}
						}
					}
				}
			}
			this.sleep(1000 * 60 * 1);
			List<Post> listPost = getIncreasePost(keyword);
			CheckMeaningIncreasePostThread CheckMeaningIncreasePostThread = new CheckMeaningIncreasePostThread(client,
					keyword, listPost, crisisService, commentService, keywordService, notificationService,
					notificationContentService, userInfoService, userService, postService, listCrisis,
					notificationTokenService);
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
			NotificationTokenService notificationTokenService) {
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
		double reactArray[] = new double[listPost.size()];
		double shareArray[] = new double[listPost.size()];
		double commentArray[] = new double[listPost.size()];
		for (int i = 0; i < listPost.size(); i = i + 2) {
			Post lastPost = listPost.get(i);
			Post newPost = listPost.get(i + 1);
			reactArray[i] = newPost.getNumberOfReact() - lastPost.getNumberOfReact();
			shareArray[i] = newPost.getNumberOfReweet() - lastPost.getNumberOfReweet();
			commentArray[i] = newPost.getNumberOfReply() - lastPost.getNumberOfReply();
		}
		double reactStandart = calculateSD(reactArray);
		double reactMean = mean(reactArray);
		double react_anomaly_cut_off = reactStandart * 2;
		double react_lower_limit = reactMean - react_anomaly_cut_off;
		double react_upper_limit = reactMean + react_anomaly_cut_off;

		double shareStandart = calculateSD(shareArray);
		double shareMean = mean(shareArray);
		double share_anomaly_cut_off = shareStandart * 2;
		double share_lower_limit = shareMean - share_anomaly_cut_off;
		double share_upper_limit = shareMean + share_anomaly_cut_off;

		double commentStandart = calculateSD(commentArray);
		double commentMean = mean(commentArray);
		double comment_anomaly_cut_off = commentStandart * 2;
		double comment_lower_limit = commentMean - comment_anomaly_cut_off;
		double comment_upper_limit = commentMean + comment_anomaly_cut_off;
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
				builder.setText(post.getPostContent());
				EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
				List<EntitiySentiments> list = elsa.getEntitiySentiments();
				countHit += entity_sentiment_count;
				if (list.size() > 0) {
					for (int x = 0; x < list.size(); x++) {
						EntitiySentiments sen = list.get(x);
						String mean = sen.getOverallSentiment().getPolarity();
						float confidence = sen.getOverallSentiment().getConfidence();
						String word = sen.getMentions()[0].getText();
						if (mean.equals(negative) && confidence > lowerConfidence
								&& word.toLowerCase().equals(keyword.toLowerCase())) {
							if (reactMean < lowMean && shareMean < lowMean && commentMean < lowMean) {
								if ((post.getNumberOfReply() - nextPost.getNumberOfReply()) > comment_upper_limit
										|| (post.getNumberOfReweet() - nextPost.getNumberOfReweet()) > share_upper_limit
										|| (post.getNumberOfReact()
												- nextPost.getNumberOfReact()) > react_upper_limit) {
									// Add Crisis To Db
									insertPostCrisis(nextPost, crisisService);
								} else {
									// Save crisis and check if already add or not
									if (reactStandart < lowStandard || shareStandart < lowStandard
											|| commentStandart < lowStandard) {
										if ((post.getNumberOfReply() - nextPost.getNumberOfReply()) > commentMean
												|| (post.getNumberOfReweet() - nextPost.getNumberOfReweet()) > shareMean
												|| (post.getNumberOfReact()
														- nextPost.getNumberOfReact()) > reactMean) {
											insertPostCrisis(nextPost, crisisService);
										}
									} else {
										insertPostCrisis(nextPost, crisisService);
									}
								}
							}
						}
					}
				}
			}
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
					notificationContentService, userInfoService, userService, commentService, postService, listCrisis);
			CheckMeaningIncreaseCommentThread.start();
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
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
			UserService userService, CommentService commentService, PostService postService, List<Crisis> listCrisis) {
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
		int size = 0;
		double reactArray[] = new double[listComment.size() / 2];
		double commentArray[] = new double[listComment.size() / 2];
		for (int i = 0; i < listComment.size(); i = i + 2) {
			Comment lastComment = listComment.get(i);
			Comment newComment = listComment.get(i + 1);
			reactArray[i] = newComment.getNumberOfReact() - lastComment.getNumberOfReact();
			commentArray[i] = newComment.getNumberOfReply() - lastComment.getNumberOfReply();
		}
		double reactStandart = calculateSD(reactArray);
		double reactMean = mean(reactArray);
		double react_anomaly_cut_off = reactStandart * 2;
		double react_lower_limit = reactMean - react_anomaly_cut_off;
		double react_upper_limit = reactMean + react_anomaly_cut_off;

		double commentStandart = calculateSD(commentArray);
		double commentMean = mean(commentArray);
		double comment_anomaly_cut_off = commentStandart * 2;
		double comment_lower_limit = commentMean - comment_anomaly_cut_off;
		double comment_upper_limit = commentMean + comment_anomaly_cut_off;
		try {
			for (int i = 0; i < listComment.size(); i += 2) {
				if (totalCount - countHit < entity_sentiment_count) {
					countHit = 0;
					this.sleep(1000 * 60 * 1);
				}
				Comment lastComment = listComment.get(i);
				Comment newComment = listComment.get(i + 1);
				builder.setText(newComment.getCommentContent());
				EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
				List<EntitiySentiments> list = elsa.getEntitiySentiments();
				countHit += entity_sentiment_count;
				if (list.size() > 0) {
					for (int x = 0; x < list.size(); x++) {
						EntitiySentiments sen = list.get(x);
						String mean = sen.getOverallSentiment().getPolarity();
						float confidence = sen.getOverallSentiment().getConfidence();
						String word = sen.getMentions()[0].getText();
						if (mean.equals(negative) && confidence > lowerConfidence
								&& word.toLowerCase().equals(keyword.toLowerCase())) {
							if (reactMean < lowMean && commentMean < lowMean) {
								if ((lastComment.getNumberOfReply()
										- newComment.getNumberOfReply()) > comment_upper_limit
										|| (lastComment.getNumberOfReact()
												- newComment.getNumberOfReact()) > react_upper_limit) {
									// Add Crisis to Db
									insertCommentCrisis(newComment, crisisService);
								}
							} else {
								if (reactStandart < lowStandard || commentStandart < lowStandard) {
									if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) > commentMean
											|| (lastComment.getNumberOfReact()
													- newComment.getNumberOfReact()) > reactMean) {
										insertCommentCrisis(newComment, crisisService);
									}
								} else {
									insertCommentCrisis(newComment, crisisService);
								}
							}
						}
					}
				} else {
					SentimentParams.Builder sentimentBuilder = SentimentParams.newBuilder();
					sentimentBuilder.setText(newComment.getCommentContent());
					sentimentBuilder.setMode("tweet");
					Sentiment sentiment = client.sentiment(sentimentBuilder.build());
					countHit += sentiment_count;
					if (sentiment.getPolarity().equals(negative)
							&& sentiment.getPolarityConfidence() > lowerConfidence) {
						if (reactMean < lowMean && commentMean < lowMean) {
							if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) > comment_upper_limit
									|| (lastComment.getNumberOfReact()
											- newComment.getNumberOfReact()) > react_upper_limit) {
								Crisis result = crisisService.findCrisis(newComment.getCommentId(), "comment", keyword);
								insertCommentCrisis(newComment, crisisService);
							}
						} else {
							if (reactStandart < lowStandard || commentStandart < lowStandard) {
								if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) > commentMean
										|| (lastComment.getNumberOfReact()
												- newComment.getNumberOfReact()) > reactMean) {
									insertCommentCrisis(newComment, crisisService);
								}
							} else {
								insertCommentCrisis(newComment, crisisService);
							}
						}
					}
				}
			}
			this.sleep(1000 * 60 * 1);
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
