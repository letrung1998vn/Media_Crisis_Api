package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.EntityLevelSentimentParams;
import com.aylien.textapi.responses.EntitiesSentiment;
import com.aylien.textapi.responses.EntitiySentiments;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.BaseThread;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.LastStandardService;
import fpt.capstone.betatest.services.NotificationService;

@Service
public class CheckMeaningIncreasePostService extends BaseThread{
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private LastStandardService lastStandardService;
	
	@Autowired
	private CrisisService crisisService;
	
	@Autowired
	private CommentService commentService;
	
	public void setData(TextAPIClient client, String keyword, List<Post> listPost, List<Crisis> listCrisis) {
		this.client = client;
		this.keyword = keyword;
		this.listPost = listPost;
		this.listCrisis = listCrisis;
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
				react_upper_limit = lastStandardService.calUpperLimit(lastPostStandardReact.getLastStandard(), lastPostStandardReact.getLastMean());
			}
			if (lastPostStandardShare != null) {
				share_upper_limit = lastStandardService.calUpperLimit(lastPostStandardShare.getLastStandard(),  lastPostStandardShare.getLastMean());
			}
			if (lastPostStandardComment != null) {
				comment_upper_limit = lastStandardService.calUpperLimit(lastPostStandardComment.getLastStandard(), lastPostStandardComment.getLastMean());
			}
			try {
				if (listPost.size() < 2) {
					if (listCrisis.size() > 0) {
						notificationService.sendNotification(listCrisis, keyword);
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
								if ((post.getNumberOfReply() - nextPost.getNumberOfReply()) > comment_upper_limit
										|| (post.getNumberOfReweet() - nextPost.getNumberOfReweet()) > share_upper_limit
										|| (post.getNumberOfReact()
												- nextPost.getNumberOfReact()) > react_upper_limit) {
									// Add Crisis To Db
									crisisService.insertPostCrisis(nextPost, word, type, listCrisis);;
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
				CheckMeaningIncreaseCommentService CheckMeaningIncreaseCommentThread = new CheckMeaningIncreaseCommentService();
				CheckMeaningIncreaseCommentThread.setData(client, keyword, listComment, listCrisis);
				CheckMeaningIncreaseCommentThread.start();
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
