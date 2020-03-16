
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
		// DetectCrisisInCurrent(keyword, client);
		DetectCrisisIncrease(keyword, client);
		// List<Keyword> listRelateKeyword = getRelate(keyword);
		// for (int a = 0; a < listRelateKeyword.size(); a++) {
		// Keyword relateKeyword = listRelateKeyword.get(a);
		// checkMeaningRelate(relateKeyword.getKeyword(), client);
		// }
	}

	private void DetectCrisisInCurrent(String keyword, TextAPIClient client) throws Exception {
		List<Post> listPost = getRecentPost(keyword);
		List<Comment> listComment = new ArrayList<>();
		for (int i = 0; i < listPost.size(); i++) {
			if (i < listPost.size() - 1) {
				Post post = listPost.get(i);
				Post nextPost = listPost.get(i + 1);
				if (post.getPostContent().equals(nextPost.getPostContent())) {
					listComment.addAll(getIncreaseComment(post.getId()));
				} else {

				}
			} else {

			}
		}
		checkMeaningPost(keyword, client, listPost);
		checkMeaningComment(listComment, keyword, client);
	}

	private void checkMeaningPost(String keyword, TextAPIClient client, List<Post> listPost) throws Exception {
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
		for (int i = 0; i < listPost.size(); i++) {
			Post post = listPost.get(i);
			builder.setText(post.getPostContent());
			EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
			List<EntitiySentiments> list = elsa.getEntitiySentiments();
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

	private void checkMeaningComment(List<Comment> listComment, String keyword, TextAPIClient client) throws Exception {
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
		for (int i = 0; i < listComment.size(); i++) {
			Comment comment = listComment.get(i);
			builder.setText(comment.getCommentContent());
			EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
			List<EntitiySentiments> list = elsa.getEntitiySentiments();
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
			} else {
				SentimentParams.Builder sentimentBuilder = SentimentParams.newBuilder();
				sentimentBuilder.setText(comment.getCommentContent());
				sentimentBuilder.setMode("tweet");
				Sentiment sentiment = client.sentiment(sentimentBuilder.build());
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

	private void DetectCrisisIncrease(String keyword, TextAPIClient client) throws Exception {
		List<Post> listPost = getIncreasePost(keyword);
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
			int result=findComment(newPostComment, lastComment);
			if(result!=0) {
				listComment.add(lastComment);
				listComment.add(newPostComment.get(i));
			}
		}
		// CheckMeaningIncreasePost(keyword, client, listPost);
		checkMeaningIncreaseComment(listComment, keyword, client);
	}
	private int findComment(List<Comment> listComment, Comment comment) {
		for(int i=0;i<listComment.size();i++) {
			if(listComment.get(i).getCommentContent().equals(comment.getCommentContent())) {
				return i;
			}
		}
		return 0;
	}
	private void CheckMeaningIncreasePost(String keyword, TextAPIClient client, List<Post> listPost) throws Exception {
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
		for (int i = 0; i < listPost.size(); i = i + 2) {
			Post post = listPost.get(i);
			Post nextPost = listPost.get(i + 1);
			builder.setText(post.getPostContent());
			EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
			List<EntitiySentiments> list = elsa.getEntitiySentiments();
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
									|| (post.getNumberOfReact() - nextPost.getNumberOfReact()) > react_upper_limit) {
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

	private void checkMeaningIncreaseComment(List<Comment> listComment, String keyword, TextAPIClient client)
			throws Exception {
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
		double react_anomaly_cut_off = reactStandart * 3;
		double react_lower_limit = reactMean - react_anomaly_cut_off;
		double react_upper_limit = reactMean + react_anomaly_cut_off;

		double commentStandart = calculateSD(commentArray);
		double commentMean = mean(reactArray);
		double comment_anomaly_cut_off = commentStandart * 3;
		double comment_lower_limit = commentMean - comment_anomaly_cut_off;
		double comment_upper_limit = commentMean + comment_anomaly_cut_off;
		for (int i = 0; i < listComment.size(); i++) {
			Comment comment = listComment.get(i);
			builder.setText(comment.getCommentContent());
			EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
			if (elsa.getText().equals(keyword)) {
				List<EntitiySentiments> list = elsa.getEntitiySentiments();
				if (list.size() > 0) {
					for (int x = 0; x < list.size(); x++) {
						EntitiySentiments sen = list.get(x);
						String mean = sen.getOverallSentiment().getPolarity();
						float confidence = sen.getOverallSentiment().getConfidence();
						if (mean.equals("negative") && confidence > 0.3) {
							if (comment.getNumberOfReply() > comment_upper_limit
									|| comment.getNumberOfReact() > react_upper_limit) {
								// Add Crisis to Db
							}
						}
					}
				}
			} else {
				SentimentParams.Builder sentimentBuilder = SentimentParams.newBuilder();
				sentimentBuilder.setText(comment.getCommentContent());
				sentimentBuilder.setMode("tweet");
				Sentiment sentiment = client.sentiment(sentimentBuilder.build());
				if (sentiment.getPolarity().equals("negative") && sentiment.getPolarityConfidence() > 0.3) {
					if (comment.getNumberOfReply() > comment_upper_limit
							|| comment.getNumberOfReact() > react_upper_limit) {

					}
				}
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

	private List<Keyword> getRelate(String keyword) {
		List<Keyword> list = new ArrayList<>();
		return list;
	}

	private List<Post> getRecentPost(String keyword) {
		// Get The list of post with latest date in DB
		List<Post> posts = postService.getEachPostContentWithLatestDate(keyword);
		return posts;
	}

	private List<Comment> getRecentComment(BigInteger PostId) {
		// Get The list of comment with latest date in DB
		List<Comment> listComment = commentService.getCommentByPostId(PostId);
		return listComment;
	}

	private List<Post> getIncreasePost(String keyword) {
		// Get the list of post with two latest date in DB
		List<Post> listPost = postService.getPostContentWithTwoLatestDate(keyword);
		List<Post> resultList = new ArrayList<>();
		for (int i = 0; i < listPost.size(); i++) {
			if (i < listPost.size() - 1) {
				Post post = listPost.get(i);
				Post nextPost = listPost.get(i + 1);
				if (post.getPostContent().equals(nextPost.getPostContent())) {
					if (!checkExist(resultList, post.getPostContent())) {
						if (post.getCrawlDate().after(nextPost.getCrawlDate())) {
							resultList.add(post);
							resultList.add(nextPost);
						} else {
							resultList.add(nextPost);
							resultList.add(post);
						}
					}
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

	private List<Comment> getIncreaseComment(BigInteger PostId) {
		// Get the list of comment with two latest date in DB
		List<Comment> listComment = commentService.getCommentByPostId(PostId);
		return listComment;
	}

	private List<Post> getRecentPostRelate(String keyword) {
		// Get The list of relate post with latest date in DB
		List<Post> listPost = new ArrayList<>();
		return listPost;
	}

	private List<Comment> getRecentCommentRelate(String PostId) {
		// Get The list of relate comment with latest date in DB
		List<Comment> listComment = new ArrayList<>();
		return listComment;
	}

	private List<Post> getIncreasePostRelate(String keyword) {
		// Get the list of relate post with two latest date in DB
		List<Post> listPost = new ArrayList<>();
		return listPost;
	}

	private List<Comment> getIncreaseCommentRelate(String PostId) {
		// Get the list of relate comment with two latest date in DB
		List<Comment> listComment = new ArrayList<>();
		return listComment;
	}

	// private void checkMeaningRelate(String relateKeyword, TextAPIClient client)
	// throws Exception {
	// List<Post> listPost = getRecentPostRelate(relateKeyword);
	// List<Comment> listComment = new ArrayList<>();
	// for (int i = 0; i < listPost.size(); i++) {
	// Post post = listPost.get(i);
	// listComment.addAll(getRecentCommentRelate(post.getId()));
	// }
	// checkMeaningRecentPost(listPost, relateKeyword, client);
	// checkMeaningRecentComment(listComment, relateKeyword, client);
	// CheckMeaningIncreaseRelatePost(listPost, relateKeyword, client);
	// checkMeaningIncreaseRelateComment(listComment, relateKeyword, client);
	// }
	//
	// private void checkMeaningRecentPost(List<Post> listPost, String
	// relateKeyword, TextAPIClient client)
	// throws Exception {
	// double reactArray[] = new double[listPost.size()];
	// double shareArray[] = new double[listPost.size()];
	// double commentArray[] = new double[listPost.size()];
	// for (int i = 0; i < listPost.size(); i++) {
	// Post post = listPost.get(i);
	// reactArray[i] = post.getNumbet_Of_React();
	// shareArray[i] = post.getNumber_of_Share();
	// commentArray[i] = post.getNumber_Of_Comment();
	// }
	// double reactStandart = calculateSD(reactArray);
	// double reactMean = mean(reactArray);
	// double react_anomaly_cut_off = reactStandart * 3;
	// double react_lower_limit = reactMean - react_anomaly_cut_off;
	// double react_upper_limit = reactMean + react_anomaly_cut_off;
	//
	// double shareStandart = calculateSD(shareArray);
	// double shareMean = mean(reactArray);
	// double share_anomaly_cut_off = shareStandart * 3;
	// double share_lower_limit = shareMean - share_anomaly_cut_off;
	// double share_upper_limit = shareMean + share_anomaly_cut_off;
	//
	// double commentStandart = calculateSD(commentArray);
	// double commentMean = mean(reactArray);
	// double comment_anomaly_cut_off = commentStandart * 3;
	// double comment_lower_limit = commentMean - comment_anomaly_cut_off;
	// double comment_upper_limit = commentMean + comment_anomaly_cut_off;
	// for (int i = 0; i < listPost.size(); i++) {
	// Post post = listPost.get(i);
	// EntityLevelSentimentParams.Builder builder =
	// EntityLevelSentimentParams.newBuilder();
	// builder.setText(post.getContent());
	// EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
	// if (elsa.getText().equals(relateKeyword)) {
	// List<EntitiySentiments> list = elsa.getEntitiySentiments();
	// if (list.size() > 0) {
	// for (int x = 0; x < list.size(); x++) {
	// EntitiySentiments sen = list.get(x);
	// String mean = sen.getOverallSentiment().getPolarity();
	// float confidence = sen.getOverallSentiment().getConfidence();
	// if (mean.equals("negative") && confidence > 0.3) {
	// if (post.getNumber_Of_Comment() > comment_upper_limit
	// || post.getNumber_of_Share() > share_upper_limit
	// || post.getNumbet_Of_React() > react_upper_limit) {
	//
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// private void checkMeaningRecentComment(List<Comment> listComment, String
	// relateKeyword, TextAPIClient client)
	// throws Exception {
	// EntityLevelSentimentParams.Builder builder =
	// EntityLevelSentimentParams.newBuilder();
	// double reactArray[] = new double[listComment.size()];
	// double commentArray[] = new double[listComment.size()];
	// for (int i = 0; i < listComment.size(); i++) {
	// Comment comment = listComment.get(i);
	// reactArray[i] = comment.getNumbet_Of_React();
	// commentArray[i] = comment.getNumber_Of_Reply();
	// }
	// double reactStandart = calculateSD(reactArray);
	// double reactMean = mean(reactArray);
	// double react_anomaly_cut_off = reactStandart * 3;
	// double react_lower_limit = reactMean - react_anomaly_cut_off;
	// double react_upper_limit = reactMean + react_anomaly_cut_off;
	//
	// double commentStandart = calculateSD(commentArray);
	// double commentMean = mean(reactArray);
	// double comment_anomaly_cut_off = commentStandart * 3;
	// double comment_lower_limit = commentMean - comment_anomaly_cut_off;
	// double comment_upper_limit = commentMean + comment_anomaly_cut_off;
	// for (int i = 0; i < listComment.size(); i++) {
	// Comment comment = listComment.get(i);
	// builder.setText(comment.getContent());
	// EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
	// if (elsa.getText().equals(relateKeyword)) {
	// List<EntitiySentiments> list = elsa.getEntitiySentiments();
	// if (list.size() > 0) {
	// for (int x = 0; x < list.size(); x++) {
	// EntitiySentiments sen = list.get(x);
	// String mean = sen.getOverallSentiment().getPolarity();
	// float confidence = sen.getOverallSentiment().getConfidence();
	// if (mean.equals("negative") && confidence > 0.3) {
	// if (comment.getNumber_Of_Reply() > comment_upper_limit
	// || comment.getNumbet_Of_React() > react_upper_limit) {
	//
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// private void CheckMeaningIncreaseRelatePost(List<Post> listPost, String
	// keyword, TextAPIClient client)
	// throws Exception {
	// EntityLevelSentimentParams.Builder builder =
	// EntityLevelSentimentParams.newBuilder();
	// double reactArray[] = new double[listPost.size()];
	// double shareArray[] = new double[listPost.size()];
	// double commentArray[] = new double[listPost.size()];
	// for (int i = 0; i < listPost.size(); i = i + 2) {
	// Post lastPost = listPost.get(i);
	// Post newPost = listPost.get(i + 1);
	// reactArray[i] = newPost.getNumbet_Of_React() - lastPost.getNumbet_Of_React();
	// shareArray[i] = newPost.getNumber_of_Share() - lastPost.getNumber_of_Share();
	// commentArray[i] = newPost.getNumber_Of_Comment() -
	// lastPost.getNumber_Of_Comment();
	// }
	// double reactStandart = calculateSD(reactArray);
	// double reactMean = mean(reactArray);
	// double react_anomaly_cut_off = reactStandart * 3;
	// double react_lower_limit = reactMean - react_anomaly_cut_off;
	// double react_upper_limit = reactMean + react_anomaly_cut_off;
	//
	// double shareStandart = calculateSD(shareArray);
	// double shareMean = mean(reactArray);
	// double share_anomaly_cut_off = shareStandart * 3;
	// double share_lower_limit = shareMean - share_anomaly_cut_off;
	// double share_upper_limit = shareMean + share_anomaly_cut_off;
	//
	// double commentStandart = calculateSD(commentArray);
	// double commentMean = mean(reactArray);
	// double comment_anomaly_cut_off = commentStandart * 3;
	// double comment_lower_limit = commentMean - comment_anomaly_cut_off;
	// double comment_upper_limit = commentMean + comment_anomaly_cut_off;
	// for (int i = 0; i < listPost.size(); i++) {
	// Post post = listPost.get(i);
	// builder.setText(post.getContent());
	// EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
	// if (elsa.getText().equals(keyword)) {
	// List<EntitiySentiments> list = elsa.getEntitiySentiments();
	// if (list.size() > 0) {
	// for (int x = 0; x < list.size(); x++) {
	// EntitiySentiments sen = list.get(x);
	// String mean = sen.getOverallSentiment().getPolarity();
	// float confidence = sen.getOverallSentiment().getConfidence();
	// if (mean.equals("negative") && confidence > 0.3) {
	// if (post.getNumber_Of_Comment() > comment_upper_limit
	// || post.getNumber_of_Share() > share_upper_limit
	// || post.getNumbet_Of_React() > react_upper_limit) {
	// // Add Crisis To Db
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// private void checkMeaningIncreaseRelateComment(List<Comment> listComment,
	// String keyword, TextAPIClient client)
	// throws Exception {
	// EntityLevelSentimentParams.Builder builder =
	// EntityLevelSentimentParams.newBuilder();
	// double reactArray[] = new double[listComment.size()];
	// double commentArray[] = new double[listComment.size()];
	// for (int i = 0; i < listComment.size(); i = i + 2) {
	// Comment lastComment = listComment.get(i);
	// Comment newComment = listComment.get(i);
	// reactArray[i] = newComment.getNumbet_Of_React() -
	// lastComment.getNumbet_Of_React();
	// commentArray[i] = newComment.getNumber_Of_Reply() -
	// lastComment.getNumber_Of_Reply();
	// }
	// double reactStandart = calculateSD(reactArray);
	// double reactMean = mean(reactArray);
	// double react_anomaly_cut_off = reactStandart * 3;
	// double react_lower_limit = reactMean - react_anomaly_cut_off;
	// double react_upper_limit = reactMean + react_anomaly_cut_off;
	//
	// double commentStandart = calculateSD(commentArray);
	// double commentMean = mean(reactArray);
	// double comment_anomaly_cut_off = commentStandart * 3;
	// double comment_lower_limit = commentMean - comment_anomaly_cut_off;
	// double comment_upper_limit = commentMean + comment_anomaly_cut_off;
	// for (int i = 0; i < listComment.size(); i++) {
	// Comment comment = listComment.get(i);
	// builder.setText(comment.getContent());
	// EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
	// if (elsa.getText().equals(keyword)) {
	// List<EntitiySentiments> list = elsa.getEntitiySentiments();
	// if (list.size() > 0) {
	// for (int x = 0; x < list.size(); x++) {
	// EntitiySentiments sen = list.get(x);
	// String mean = sen.getOverallSentiment().getPolarity();
	// float confidence = sen.getOverallSentiment().getConfidence();
	// if (mean.equals("negative") && confidence > 0.3) {
	// if (comment.getNumber_Of_Reply() > comment_upper_limit
	// || comment.getNumbet_Of_React() > react_upper_limit) {
	// // Add Crisis to Db
	// }
	// }
	// }
	// }
	// }
	// }
	// }
}