package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.EntityLevelSentimentParams;
import com.aylien.textapi.parameters.SentimentParams;
import com.aylien.textapi.responses.EntitiesSentiment;
import com.aylien.textapi.responses.EntitiySentiments;
import com.aylien.textapi.responses.Sentiment;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.Post;

@Service
public class CheckMeaningService {

	@Autowired
	private CommentService commentService;

	@Autowired
	private PostService postService;

	@Autowired
	private LastStandardService lastStandardService;

	@Autowired
	private CheckMeaningCurrentPostService CheckMeaningCurrentPostThread;

	public final double lowerConfidence = 0.5;
	public final String negative = "negative";

	@Transactional
	public void calStandard(String keyword) {
		List<Post> listPost = postService.getNewPost(keyword, true);
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
				listSencondComment.add(sencondCommentInDb);
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

	@Transactional
	public void calCommentStandard(String keyword, List<Comment> listComment) {
		LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "comment", "react");
		LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "comment", "comment");
		if (lastCommentStandardReact != null && lastCommentStandardComment != null) {
			if (listComment.size() > 0) {

				double totalNewReact = 0;
				double totalNewComment = 0;

				double newReactVariance = 0;
				double newCommentVariance = 0;

				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					totalNewReact += comment.getNumberOfReact();
					totalNewComment += comment.getNumberOfReply();
				}

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
					newReactVariance += Math.pow(comment.getNumberOfReact() - reactMean, 2);
					newCommentVariance += Math.pow(comment.getNumberOfReply() - commentMean, 2);
				}
				double leftSideStandardReact = totalNewCommentReact
						* (Math.pow(reactMean - lastCommentStandardReact.getLastMean(), 2)
								+ Math.pow(lastCommentStandardReact.getLastStandard(), 2));
				double leftSideStandardComment = totalNewCommentComment
						* (Math.pow(reactMean - lastCommentStandardComment.getLastMean(), 2)
								+ Math.pow(lastCommentStandardComment.getLastStandard(), 2));

				double commentReactVariance = leftSideStandardReact + newReactVariance;
				double newCommentReactStandard = Math.sqrt((commentReactVariance) / (totalNewCommentReact));

				double postCommentVariance = leftSideStandardComment + newCommentVariance;
				double newCommentCommentStandard = Math.sqrt((postCommentVariance) / (totalNewCommentComment));

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

	@Transactional
	public void calPostStandard(String keyword, List<Post> listPost) {
		LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "post", "react");
		LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "post", "share");
		LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "post", "comment");
		double newReactVariance = 0;
		double newShareVariance = 0;
		double newCommentVariance = 0;
		if (lastPostStandardReact != null && lastPostStandardShare != null && lastPostStandardComment != null) {
			if (listPost.size() > 0) {

				double totalNewReact = 0;
				double totalNewShare = 0;
				double totalNewComment = 0;
				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					totalNewReact += post.getNumberOfReact();
					totalNewShare += post.getNumberOfReweet();
					totalNewComment += post.getNumberOfReply();
				}

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
					newReactVariance += Math.pow(post.getNumberOfReact() - reactMean, 2);
					newShareVariance += Math.pow(post.getNumberOfReweet() - shareMean, 2);
					newCommentVariance += Math.pow(post.getNumberOfReply() - commentMean, 2);
				}
				double leftSideStandardReact = totalNewPostReact
						* (Math.pow(reactMean - lastPostStandardReact.getLastMean(), 2)
								+ Math.pow(lastPostStandardReact.getLastStandard(), 2));
				double leftSideStandardShare = totalNewPostShare
						* (Math.pow(reactMean - lastPostStandardShare.getLastMean(), 2)
								+ Math.pow(lastPostStandardShare.getLastStandard(), 2));
				double leftSideStandardComment = totalNewPostComment
						* (Math.pow(reactMean - lastPostStandardComment.getLastMean(), 2)
								+ Math.pow(lastPostStandardComment.getLastStandard(), 2));

				double postReactVariance = leftSideStandardReact + newReactVariance;
				double newPostReactStandard = Math.sqrt((postReactVariance) / (totalNewPostReact));

				double postShareVariance = leftSideStandardShare + newShareVariance;
				double newPostShareStandard = Math.sqrt((postShareVariance) / (totalNewPostShare));

				double postCommentVariance = leftSideStandardComment + newCommentVariance;
				double newPostCommentStandard = Math.sqrt((postCommentVariance) / (totalNewPostComment));

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

	@Transactional
	public void calIncreaseCommentStandard(String keyword, List<Comment> listComment, List<Comment> listSecondComment) {
		LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "increaseComment",
				"react");
		LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "increaseComment",
				"comment");
		if (lastCommentStandardReact != null && lastCommentStandardComment != null) {
			if (listComment.size() > 0) {
				double newReactVariance = 0;
				double newCommentVariance = 0;
				double totalNewReact = 0;
				double totalNewComment = 0;

				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					int pos = getSameCommentPos(listSecondComment, comment);
					Comment secondComment = listSecondComment.get(pos);
					totalNewReact += comment.getNumberOfReact() - secondComment.getNumberOfReact();
					totalNewComment += comment.getNumberOfReply() - secondComment.getNumberOfReply();
				}

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
					int pos = getSameCommentPos(listSecondComment, comment);
					Comment secondComment = listSecondComment.get(pos);
					newReactVariance += Math
							.pow(comment.getNumberOfReact() - secondComment.getNumberOfReact() - reactMean, 2);
					newCommentVariance += Math
							.pow(comment.getNumberOfReply() - secondComment.getNumberOfReply() - commentMean, 2);
				}

				double leftSideStandardReact = totalNewCommentReact
						* (Math.pow(reactMean - lastCommentStandardReact.getLastMean(), 2)
								+ Math.pow(lastCommentStandardReact.getLastStandard(), 2));
				double leftSideStandardComment = totalNewCommentComment
						* (Math.pow(reactMean - lastCommentStandardComment.getLastMean(), 2)
								+ Math.pow(lastCommentStandardComment.getLastStandard(), 2));

				double commentReactVariance = leftSideStandardReact + newReactVariance;
				double newCommentReactStandard = Math.sqrt((commentReactVariance) / (totalNewCommentReact));

				double postCommentVariance = leftSideStandardComment + newCommentVariance;
				double newCommentCommentStandard = Math.sqrt((postCommentVariance) / (totalNewCommentComment));

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
					if (pos != -1) {
						Comment secondComment = listSecondComment.get(pos);
						reactArray[i] = comment.getNumberOfReact() - secondComment.getNumberOfReact();
						commentArray[i] = comment.getNumberOfReply() - secondComment.getNumberOfReply();
					}
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

	@Transactional
	public int getSameCommentPos(List<Comment> listComment, Comment comment) {
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

	@Transactional
	public void calIncreasePostStandard(String keyword, List<Post> listPost, List<Post> listSecondPost) {
		LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "increasePost", "react");
		LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "increasePost", "share");
		LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "increasePost", "comment");

		if (lastPostStandardReact != null && lastPostStandardShare != null && lastPostStandardComment != null) {
			if (listPost.size() > 0) {

				double newReactVariance = 0;
				double newShareVariance = 0;
				double newCommentVariance = 0;

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
					newReactVariance += Math.pow(post.getNumberOfReact() - secondPost.getNumberOfReact() - reactMean,
							2);
					newShareVariance += Math.pow(post.getNumberOfReweet() - secondPost.getNumberOfReweet() - shareMean,
							2);
					newCommentVariance += Math
							.pow(post.getNumberOfReply() - secondPost.getNumberOfReply() - commentMean, 2);
				}
				double leftSideStandardReact = totalNewPostReact
						* (Math.pow(reactMean - lastPostStandardReact.getLastMean(), 2)
								+ Math.pow(lastPostStandardReact.getLastStandard(), 2));
				double leftSideStandardShare = totalNewPostShare
						* (Math.pow(reactMean - lastPostStandardShare.getLastMean(), 2)
								+ Math.pow(lastPostStandardShare.getLastStandard(), 2));
				double leftSideStandardComment = totalNewPostComment
						* (Math.pow(reactMean - lastPostStandardComment.getLastMean(), 2)
								+ Math.pow(lastPostStandardComment.getLastStandard(), 2));

				double postReactVariance = leftSideStandardReact + newReactVariance;
				double newPostReactStandard = Math.sqrt((postReactVariance) / (totalNewPostReact));

				double postShareVariance = leftSideStandardShare + newShareVariance;
				double newPostShareStandard = Math.sqrt((postShareVariance) / (totalNewPostShare));

				double postCommentVariance = leftSideStandardComment + newCommentVariance;
				double newPostCommentStandard = Math.sqrt((postCommentVariance) / (totalNewPostComment));

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

	@Transactional
	public static double calculateSD(double numArray[]) {
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

	@Transactional
	public static double mean(double[] m) {
		double sum = 0;
		for (int i = 0; i < m.length; i++) {
			sum += m[i];
		}
		return sum / m.length;
	}

	@Transactional
	public void detectCrisisInCurrent(String keyword, TextAPIClient client, List<Crisis> listCrisis) throws Exception {
		List<Post> listPost = postService.getRecentPost(keyword);
		if (listPost.size() > 0) {
			CheckMeaningCurrentPostThread.setData(client, keyword, listPost, listCrisis);
			CheckMeaningCurrentPostThread.start();
		}
	}

	@Transactional
	public boolean checkExist(List<Post> listPost, String postContent) {
		for (int i = 0; i < listPost.size(); i++) {
			String content = listPost.get(i).getPostContent();
			if (postContent.equals(content)) {
				return true;
			}
		}
		return false;
	}

	@Transactional
	public List<Post> getListSameContent(List<Post> listPost, Post checkPost) {
		List<Post> result = new ArrayList<>();
		for (int i = 0; i < listPost.size(); i++) {
			Post post = listPost.get(i);
			if (checkPost.getPostContent().equals(post.getPostContent())) {
				result.add(post);
			}
		}
		return result;
	}

	@Transactional
	public List<Post> sortByCrawlDate(List<Post> listPost) {
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

	@Transactional
	public Post updateMeaningPost(Post post, TextAPIClient client, String keyword) {
		boolean flag = false;
		try {
			EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
			builder.setText(post.getPostContent());
			EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
			List<EntitiySentiments> list = elsa.getEntitiySentiments();
			if (list.size() > 0) {
				for (int x = 0; x < list.size(); x++) {
					EntitiySentiments sen = list.get(x);
					String word = sen.getMentions()[0].getText();
					String mean = sen.getOverallSentiment().getPolarity();
					float confidence = sen.getOverallSentiment().getConfidence();
					if (mean.equals(negative) && confidence > lowerConfidence
							&& word.toLowerCase().equals(keyword.toLowerCase())) {
						flag = true;
						break;
					}
				}
			}
			if (flag) {
				post.setNegative(true);
				post = postService.save(post);
			} else {
				post.setNegative(false);
				post = postService.save(post);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return post;
	}

	@Transactional
	public Comment updateMeaningComment(Comment comment, TextAPIClient client, String keyword) {
		boolean flag = false;
		try {
			EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
			builder.setText(comment.getCommentContent());
			EntitiesSentiment elsa = client.entityLevelSentiment(builder.build());
			List<EntitiySentiments> list = elsa.getEntitiySentiments();
			if (list.size() > 0) {
				for (int x = 0; x < list.size(); x++) {
					EntitiySentiments sen = list.get(x);
					String word = sen.getMentions()[0].getText();
					String mean = sen.getOverallSentiment().getPolarity();
					float confidence = sen.getOverallSentiment().getConfidence();
					if (mean.equals(negative) && confidence > lowerConfidence
							&& word.toLowerCase().equals(keyword.toLowerCase())) {
						flag = true;
						break;
					}
				}
				if (flag) {
					comment.setNegative(true);
					comment = commentService.save(comment);
				} else {
					comment.setNegative(false);
					comment = commentService.save(comment);
				}
			} else {
				SentimentParams.Builder sentimentBuilder = SentimentParams.newBuilder();
				sentimentBuilder.setText(comment.getCommentContent());
				sentimentBuilder.setMode("tweet");
				Sentiment sentiment = client.sentiment(sentimentBuilder.build());
				if (sentiment.getPolarity().equals(negative) && sentiment.getPolarityConfidence() > lowerConfidence) {
					comment.setNegative(true);
					comment = commentService.save(comment);
				} else {
					comment.setNegative(false);
					comment = commentService.save(comment);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return comment;
	}
}
