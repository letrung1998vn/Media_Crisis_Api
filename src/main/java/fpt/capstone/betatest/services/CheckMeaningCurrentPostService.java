package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.EntityLevelSentimentParams;
import com.aylien.textapi.responses.EntitiesSentiment;
import com.aylien.textapi.responses.EntitiySentiments;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.NegativeRatio;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.BaseThread;
import fpt.capstone.betatest.services.CommentService;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.LastStandardService;
import fpt.capstone.betatest.services.NegativeRatioService;
import fpt.capstone.betatest.services.NotificationService;

@Service
public class CheckMeaningCurrentPostService extends BaseThread {

	@Autowired
	private LastStandardService lastStandardService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NegativeRatioService negativeRatioService;

	@Autowired
	private CrisisService crisisService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private CheckMeaningCurrentCommentService CheckMeaningCurrentCommentThread;

	public void setData(TextAPIClient client, String keyword, List<Post> listPost, List<Crisis> listCrisis) {
		this.client = client;
		this.keyword = keyword;
		this.listPost = listPost;
		this.listCrisis = listCrisis;
	}

	@Override
	public synchronized void start() {
		LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "post", "react");
		LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "post", "share");
		LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "post", "comment");

		double react_upper_limit = lastStandardService.calUpperLimit(lastPostStandardReact.getLastStandard(),
				lastPostStandardReact.getLastMean());

		double share_upper_limit = lastStandardService.calUpperLimit(lastPostStandardShare.getLastStandard(),
				lastPostStandardShare.getLastMean());

		double comment_upper_limit = lastStandardService.calUpperLimit(lastPostStandardComment.getLastStandard(),
				lastPostStandardComment.getLastMean());
		try {
			List<Post> listPostNegative = new ArrayList<>();
			EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();

			if (listPost.size() == 0) {
				if (listCrisis.size() > 0) {
					notificationService.sendNotification(listCrisis, keyword);
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
							listPostNegative.add(post);
							if (post.getNumberOfReply() > comment_upper_limit
									|| post.getNumberOfReweet() > share_upper_limit
									|| post.getNumberOfReact() > react_upper_limit) {
								// Save crisis and check if already add or not
								listCrisis = crisisService.insertPostCrisis(post, keyword, postType, listCrisis);
							}
						}
					}
				}
			}
			double negativeRatio = negativeRatioService.calNegativeRatio(listPost.size(), listPostNegative.size());
			NegativeRatio lastNegativeRatio = negativeRatioService.getNegativeRatio(keyword, "post");
			long millis = System.currentTimeMillis();
			Date date = new Date(millis);
			boolean isNegativeIncrease = false;
			if (lastNegativeRatio != null) {
				if (lastNegativeRatio.getUpdateDate().before(date)) {
					long diffInMillies = Math.abs(date.getTime() - lastNegativeRatio.getUpdateDate().getTime());
					long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
					if (diff > differenceHour) {
						if (lastNegativeRatio.getRatio() < negativeRatio) {
							if (negativeRatio - lastNegativeRatio.getRatio() > ratioLimit) {
								lastNegativeRatio.setRatio(negativeRatio);
								lastNegativeRatio.setUpdateDate(date);
								negativeRatioService.save(lastNegativeRatio);
								isNegativeIncrease = true;
							} else {
								lastNegativeRatio.setRatio(negativeRatio);
								lastNegativeRatio.setUpdateDate(date);
								negativeRatioService.save(lastNegativeRatio);
							}
						} else {
							lastNegativeRatio.setRatio(negativeRatio);
							lastNegativeRatio.setUpdateDate(date);
							negativeRatioService.save(lastNegativeRatio);
						}
					}
				}
			} else {
				lastNegativeRatio = new NegativeRatio();
				lastNegativeRatio.setKeyword(keyword);
				lastNegativeRatio.setType("post");
				lastNegativeRatio.setUpdateDate(date);
				lastNegativeRatio.setRatio(negativeRatio);
				negativeRatioService.save(lastNegativeRatio);
			}
			if (isNegativeIncrease) {
				notificationService.sendListPostNotification(listPost, keyword);
			}
			Thread.sleep(1000 * 60 * 1);
			List<Comment> listComment = new ArrayList<>();
			for (int i = 0; i < listPost.size(); i++) {
				Post post = listPost.get(i);
				listComment.addAll(commentService.getCommentByPostId(post.getId()));
			}
			CheckMeaningCurrentCommentThread.setData(client, keyword, listComment, listCrisis);
			CheckMeaningCurrentCommentThread.start();
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
