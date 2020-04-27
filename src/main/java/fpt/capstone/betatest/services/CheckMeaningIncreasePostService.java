package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.EntityLevelSentimentParams;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.BaseThread;

@Service
public class CheckMeaningIncreasePostService extends BaseThread {
	@Autowired
	private NotificationService notificationService;

	@Autowired
	private LastStandardService lastStandardService;

	@Autowired
	private CrisisService crisisService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private CheckMeaningIncreaseCommentService CheckMeaningIncreaseCommentThread;

	@Autowired
	private CheckMeaningService checkMeaningService;

	public void setData(TextAPIClient client, String keyword, List<Post> listPost, List<Crisis> listCrisis) {
		this.client = client;
		this.keyword = keyword;
		this.listPost = listPost;
		this.listCrisis = listCrisis;
	}

	@Override
	public synchronized void start() {
		boolean interruptFlag = false;
		if (listPost.size() < 2) {
			interruptFlag = true;
			if (listCrisis.size() > 0) {
				notificationService.sendNotification(listCrisis, keyword);
			}
		}
		if (!interruptFlag) {
			LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "increasePost", "react");
			LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "increasePost", "share");
			LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "increasePost",
					"comment");
			if (lastPostStandardReact != null && lastPostStandardShare != null && lastPostStandardComment != null) {
				double react_upper_limit = 0, share_upper_limit = 0, comment_upper_limit = 0;
				if (lastPostStandardReact != null) {
					react_upper_limit = lastStandardService.calUpperLimit(lastPostStandardReact.getLastStandard(),
							lastPostStandardReact.getLastMean());
				}
				if (lastPostStandardShare != null) {
					share_upper_limit = lastStandardService.calUpperLimit(lastPostStandardShare.getLastStandard(),
							lastPostStandardShare.getLastMean());
				}
				if (lastPostStandardComment != null) {
					comment_upper_limit = lastStandardService.calUpperLimit(lastPostStandardComment.getLastStandard(),
							lastPostStandardComment.getLastMean());
				}
				try {
					long startMillis = System.currentTimeMillis();
					Date startDate = new Date(startMillis);
					for (int i = 0; i < listPost.size(); i = i + 2) {
						long currentMillis = System.currentTimeMillis();
						Date currentDate = new Date(currentMillis);
						long diffInMillies = Math.abs(currentDate.getTime() - startDate.getTime());
						long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
						if (diff >= 1 && countHit != 0) {
							startDate = currentDate;
							countHit = 0;
						}
						if (countHit != 0 && totalCount - countHit < entity_sentiment_count) {
							countHit = 0;
							this.sleep(1000 * 60 * 1);
						}
						Post post = listPost.get(i);
						Post nextPost = listPost.get(i + 1);
						if (post.isNegative() == null) {
							post = checkMeaningService.updateMeaningPost(post, client, keyword);
							countHit += entity_sentiment_count;
						}
						if (nextPost.isNegative() == null) {
							nextPost = checkMeaningService.updateMeaningPost(nextPost, client, keyword);
							countHit += entity_sentiment_count;
						}
						if (post.isNegative() && nextPost.isNegative()) {
							if ((post.getNumberOfReply() - nextPost.getNumberOfReply()) > comment_upper_limit
									|| (post.getNumberOfReweet() - nextPost.getNumberOfReweet()) > share_upper_limit
									|| (post.getNumberOfReact() - nextPost.getNumberOfReact()) > react_upper_limit) {
								// Add Crisis To Db
								listCrisis = crisisService.insertPostCrisis(nextPost, keyword, postType, listCrisis);
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
						lastPostComment.addAll(commentService.getCommentByPostId(post.getId()));
						newPostComment.addAll(commentService.getCommentByPostId(nextPost.getId()));
					}
					for (int i = 0; i < lastPostComment.size(); i++) {
						Comment lastComment = lastPostComment.get(i);
						int result = commentService.findComment(newPostComment, lastComment);
						if (result != -1) {
							listComment.add(lastComment);
							listComment.add(newPostComment.get(result));
						}
					}
					CheckMeaningIncreaseCommentThread.setData(client, keyword, listComment, listCrisis);
					CheckMeaningIncreaseCommentThread.start();
					this.interrupt();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
