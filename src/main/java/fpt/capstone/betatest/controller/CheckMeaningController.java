
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
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.PostService;
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

	@GetMapping("check")
	public void checkMeaning(@RequestParam String keyword) throws Exception {
		TextAPIClient client = new TextAPIClient("43faa103", "f2aaee05b21dabe934b89bd3198801e8");
		DetectCrisisInCurrent(keyword, client);
	}

	private void DetectCrisisInCurrent(String keyword, TextAPIClient client) throws Exception {
		List<Post> listPost = getRecentPost(keyword);
		CheckMeaningCurrentPost checkMeaningCurrentPost = new CheckMeaningCurrentPost(client, keyword, listPost,
				crisisService, commentService, postService);
		checkMeaningCurrentPost.start();
	}

	private List<Post> getRecentPost(String keyword) {
		// Get The list of post with latest date in DB
		List<Post> posts = postService.getEachPostContentWithLatestDate(keyword);
		return posts;
	}
}

class CheckMeaningCurrentPost extends Thread {
	TextAPIClient client;
	String keyword;
	List<Post> listPost;
	CrisisService crisisService;
	CommentService commentService;
	int totalCount = 60;
	int entity_sentiment_count = 3;
	int sentiment_count = 1;
	int countHit = 0;
	PostService postService;

	public CheckMeaningCurrentPost(TextAPIClient client, String keyword, List<Post> listPost,
			CrisisService crisisService, CommentService commentService, PostService postService) {
		this.client = client;
		this.keyword = keyword;
		this.listPost = listPost;
		this.crisisService = crisisService;
		this.commentService = commentService;
		this.postService = postService;
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
			for (int i = 0; i < listPost.size(); i++) {
				if (totalCount - countHit < entity_sentiment_count) {
					countHit = 0;
					this.sleep(1000 * 60 * 2);
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
						if (mean.equals("negative") && confidence > 0.3
								&& word.toLowerCase().equals(keyword.toLowerCase())) {
							if (reactMean < 50000 && shareMean < 50000 && commentMean < 50000) {
								if (post.getNumberOfReply() > comment_upper_limit
										|| post.getNumberOfReweet() > share_upper_limit
										|| post.getNumberOfReact() > react_upper_limit) {
									// Save crisis and check if already add or not
									boolean result = crisisService.checkCrisisExist(post.getId(), "post");
									if (result) {
										Crisis crisis = new Crisis();
										crisis.setContentId(post.getId());
										crisis.setType("post");
										crisisService.saveCrisis(crisis);
									}
								}
							} else {
								if (post.getNumberOfReply() > commentMean || post.getNumberOfReweet() > shareMean
										|| post.getNumberOfReact() > reactMean) {
									// Save crisis and check if already add or not
									boolean result = crisisService.checkCrisisExist(post.getId(), "post");
									if (result) {
										Crisis crisis = new Crisis();
										crisis.setContentId(post.getId());
										crisis.setType("post");
										crisisService.saveCrisis(crisis);
									}
								}
							}
						}
					}
				}
			}
			Thread.sleep(1000 * 60 * 2);
			List<Comment> listComment = new ArrayList<>();
			for (int i = 0; i < listPost.size(); i++) {
				Post post = listPost.get(i);
				listComment.addAll(getRecentComment(post.getId()));
			}
			CheckMeaningCurrentComment checkMeaningCurrentComment = new CheckMeaningCurrentComment(client, keyword,
					listComment, crisisService, postService, commentService);
			checkMeaningCurrentComment.start();
			this.stop();
		} catch (Exception e) {
			e.printStackTrace();
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

	private List<Comment> getRecentComment(BigInteger PostId) {
		// Get The list of comment with latest date in DB
		List<Comment> listComment = commentService.getCommentByPostId(PostId);
		return listComment;
	}
}

class CheckMeaningCurrentComment extends Thread {
	TextAPIClient client;
	String keyword;
	List<Comment> listComment;
	CrisisService crisisService;
	CommentService commentService;
	int totalCount = 60;
	int entity_sentiment_count = 3;
	int sentiment_count = 1;
	int countHit = 0;
	PostService postService;

	public CheckMeaningCurrentComment(TextAPIClient client, String keyword, List<Comment> listComment,
			CrisisService crisisService, PostService postService, CommentService commentService) {
		this.client = client;
		this.keyword = keyword;
		this.listComment = listComment;
		this.crisisService = crisisService;
		this.postService = postService;
		this.commentService = commentService;
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
					this.sleep(1000 * 60 * 2);
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
						if (mean.equals("negative") && confidence > 0.3
								&& word.toLowerCase().equals(keyword.toLowerCase())) {
							if (reactMean < 50000 && commentMean < 50000) {
								if (comment.getNumberOfReply() > comment_upper_limit
										|| comment.getNumberOfReact() > react_upper_limit) {
									// Add Crisis To Db
									boolean result = crisisService.checkCrisisExist(comment.getId(), "comment");
									if (result) {
										Crisis crisis = new Crisis();
										crisis.setContentId(comment.getId());
										crisis.setType("comment");
										crisisService.saveCrisis(crisis);
									}
								}
							} else {
								if (comment.getNumberOfReply() > commentMean
										|| comment.getNumberOfReact() > reactMean) {
									boolean result = crisisService.checkCrisisExist(comment.getId(), "comment");
									if (result) {
										Crisis crisis = new Crisis();
										crisis.setContentId(comment.getId());
										crisis.setType("comment");
										crisisService.saveCrisis(crisis);
									}
								}
							}
						}
					}
				} else {
					if (totalCount - countHit < sentiment_count) {
						countHit = 0;
						this.sleep(1000 * 60 * 2);
					}
					SentimentParams.Builder sentimentBuilder = SentimentParams.newBuilder();
					sentimentBuilder.setText(comment.getCommentContent());
					sentimentBuilder.setMode("tweet");
					Sentiment sentiment = client.sentiment(sentimentBuilder.build());
					countHit += sentiment_count;
					if (sentiment.getPolarity().equals("negative") && sentiment.getPolarityConfidence() > 0.3) {
						if (reactMean < 50000 && commentMean < 50000) {
							if (comment.getNumberOfReply() > comment_upper_limit
									|| comment.getNumberOfReact() > react_upper_limit) {
								boolean result = crisisService.checkCrisisExist(comment.getId(), "comment");
								if (result) {
									Crisis crisis = new Crisis();
									crisis.setContentId(comment.getId());
									crisis.setType("comment");
									crisisService.saveCrisis(crisis);
								}
							}
						} else {
							if (comment.getNumberOfReply() > commentMean || comment.getNumberOfReact() > reactMean) {
								boolean result = crisisService.checkCrisisExist(comment.getId(), "comment");
								if (result) {
									Crisis crisis = new Crisis();
									crisis.setContentId(comment.getId());
									crisis.setType("comment");
									crisisService.saveCrisis(crisis);
								}
							}
						}
					}
				}
			}
			this.sleep(1000 * 60 * 2);
			List<Post> listPost = getIncreasePost(keyword);
			CheckMeaningIncreasePost checkMeaningIncreasePost = new CheckMeaningIncreasePost(client, keyword, listPost,
					crisisService, commentService);
			this.stop();
		} catch (Exception e) {
			e.printStackTrace();
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

class CheckMeaningIncreasePost extends Thread {
	TextAPIClient client;
	String keyword;
	List<Post> listPost;
	CrisisService crisisService;
	CommentService commentService;
	int totalCount = 60;
	int entity_sentiment_count = 3;
	int sentiment_count = 1;
	int countHit = 0;

	public CheckMeaningIncreasePost(TextAPIClient client, String keyword, List<Post> listPost,
			CrisisService crisisService, CommentService commentService) {
		this.client = client;
		this.keyword = keyword;
		this.listPost = listPost;
		this.crisisService = crisisService;
		this.commentService = commentService;
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
			for (int i = 0; i < listPost.size(); i = i + 2) {
				if (totalCount - countHit < entity_sentiment_count) {
					countHit = 0;
					this.sleep(1000 * 60 * 2);
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
						if (mean.equals("negative") && confidence > 0.3
								&& word.toLowerCase().equals(keyword.toLowerCase())) {
							if (reactMean < 50000 && shareMean < 50000 && commentMean < 50000) {
								if ((post.getNumberOfReply() - nextPost.getNumberOfReply()) > comment_upper_limit
										|| (post.getNumberOfReweet() - nextPost.getNumberOfReweet()) > share_upper_limit
										|| (post.getNumberOfReact()
												- nextPost.getNumberOfReact()) > react_upper_limit) {
									// Add Crisis To Db
									boolean result = crisisService.checkCrisisExist(nextPost.getId(), "post");
									if (result) {
										Crisis crisis = new Crisis();
										crisis.setContentId(nextPost.getId());
										crisis.setType("post");
										crisisService.saveCrisis(crisis);
									}
								} else {
									// Save crisis and check if already add or not
									if (post.getNumberOfReply() > commentMean || post.getNumberOfReweet() > shareMean
											|| post.getNumberOfReact() > reactMean) {
										boolean result = crisisService.checkCrisisExist(nextPost.getId(), "post");
										if (result) {
											Crisis crisis = new Crisis();
											crisis.setContentId(nextPost.getId());
											crisis.setType("post");
											crisisService.saveCrisis(crisis);
										}
									}
								}
							}
						}
					}
				}
			}
			this.sleep(100 * 60 * 2);
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
			CheckMeaningIncreaseComment checkMeaningIncreaseComment = new CheckMeaningIncreaseComment(client, keyword,
					listComment, crisisService);
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

	private List<Comment> getIncreaseComment(BigInteger PostId) {
		// Get the list of comment with two latest date in DB
		List<Comment> listComment = commentService.getCommentByPostId(PostId);
		return listComment;
	}
}

class CheckMeaningIncreaseComment extends Thread {
	TextAPIClient client;
	String keyword;
	List<Comment> listComment;
	CrisisService crisisService;
	int totalCount = 60;
	int entity_sentiment_count = 3;
	int sentiment_count = 1;
	int countHit = 0;

	public CheckMeaningIncreaseComment(TextAPIClient client, String keyword, List<Comment> listComment,
			CrisisService crisisService) {
		this.client = client;
		this.keyword = keyword;
		this.listComment = listComment;
		this.crisisService = crisisService;
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
			for (int i = 0; i < listComment.size(); i++) {
				if (totalCount - countHit < entity_sentiment_count) {
					countHit = 0;
					this.sleep(1000 * 60 * 2);
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
						if (mean.equals("negative") && confidence > 0.3
								&& word.toLowerCase().equals(keyword.toLowerCase())) {
							if (reactMean < 50000 && commentMean < 50000) {
								if ((lastComment.getNumberOfReply()
										- newComment.getNumberOfReply()) > comment_upper_limit
										|| (lastComment.getNumberOfReact()
												- newComment.getNumberOfReact()) > react_upper_limit) {
									// Add Crisis to Db
									boolean result = crisisService.checkCrisisExist(newComment.getId(), "comment");
									if (result) {
										Crisis crisis = new Crisis();
										crisis.setContentId(newComment.getId());
										crisis.setType("comment");
										crisisService.saveCrisis(crisis);
									}
								}
							} else {
								if (lastComment.getNumberOfReply() > commentMean
										|| lastComment.getNumberOfReact() > reactMean) {
									boolean result = crisisService.checkCrisisExist(newComment.getId(), "comment");
									if (result) {
										Crisis crisis = new Crisis();
										crisis.setContentId(newComment.getId());
										crisis.setType("comment");
										crisisService.saveCrisis(crisis);
									}
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
					if (sentiment.getPolarity().equals("negative") && sentiment.getPolarityConfidence() > 0.3) {
						if (reactMean < 50000 && commentMean < 50000) {
							if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) > comment_upper_limit
									|| (lastComment.getNumberOfReact()
											- newComment.getNumberOfReact()) > react_upper_limit) {
								boolean result = crisisService.checkCrisisExist(newComment.getId(), "comment");
								if (result) {
									Crisis crisis = new Crisis();
									crisis.setContentId(newComment.getId());
									crisis.setType("comment");
									crisisService.saveCrisis(crisis);
								}
							}
						} else {
							if (lastComment.getNumberOfReply() > commentMean
									|| lastComment.getNumberOfReact() > reactMean) {
								boolean result = crisisService.checkCrisisExist(newComment.getId(), "comment");
								if (result) {
									Crisis crisis = new Crisis();
									crisis.setContentId(newComment.getId());
									crisis.setType("comment");
									crisisService.saveCrisis(crisis);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}