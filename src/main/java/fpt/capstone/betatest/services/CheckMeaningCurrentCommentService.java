package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.EntityLevelSentimentParams;
import com.aylien.textapi.parameters.SentimentParams;
import com.aylien.textapi.responses.EntitiesSentiment;
import com.aylien.textapi.responses.EntitiySentiments;
import com.aylien.textapi.responses.Sentiment;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.NegativeRatio;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.BaseThread;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.LastStandardService;
import fpt.capstone.betatest.services.NegativeRatioService;
import fpt.capstone.betatest.services.NotificationService;
import fpt.capstone.betatest.services.PostService;

@Service
public class CheckMeaningCurrentCommentService extends BaseThread{

	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private LastStandardService lastStandardService;
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private NegativeRatioService negativeRatioService;
	
	@Autowired
	private CrisisService crisisService;
	
	public void setData(TextAPIClient client, String keyword, List<Comment> listComment, List<Crisis> listCrisis) {
		this.client = client;
		this.keyword = keyword;
		this.listComment = listComment;
		this.listCrisis = listCrisis;
	}
	@Override
	public synchronized void start() {
		EntityLevelSentimentParams.Builder builder = EntityLevelSentimentParams.newBuilder();
		List<Comment> listCommentNegative = new ArrayList<>();
		LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "comment", "react");
		LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "comment", "comment");

		double react_upper_limit = lastStandardService.calUpperLimit(lastCommentStandardReact.getLastStandard(), lastCommentStandardReact.getLastMean());

		double comment_upper_limit = lastStandardService.calUpperLimit(lastCommentStandardComment.getLastStandard(), lastCommentStandardComment.getLastMean());
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
							if (comment.getNumberOfReply() > comment_upper_limit
									|| comment.getNumberOfReact() > react_upper_limit) {
								crisisService.insertCommentCrisis(comment , keyword, listCrisis,type);
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
						if (comment.getNumberOfReply() > comment_upper_limit
								|| comment.getNumberOfReact() > react_upper_limit) {
							crisisService.insertCommentCrisis(comment, keyword, listCrisis, type);;
						}
					}
				}
			}
			double negativeRatio = listComment.size() / listCommentNegative.size();
			NegativeRatio lastNegativeRatio = negativeRatioService.getNegativeRatio(keyword, "comment");
			long millis = System.currentTimeMillis();
			Date date = new Date(millis);
			boolean isNegativeIncrease = false;
			if (lastNegativeRatio != null) {
				if (lastNegativeRatio.getUpdateDate().before(date)) {
					long diffInMillies = Math.abs(date.getTime() - lastNegativeRatio.getUpdateDate().getTime());
					long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
					if (diff > differenceHour) {
						if (lastNegativeRatio.getRatio() < negativeRatio) {
							if (negativeRatio - lastNegativeRatio.getRatio() < ratioLimit) {
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
				lastNegativeRatio.setType("comment");
				lastNegativeRatio.setUpdateDate(date);
				lastNegativeRatio.setRatio(negativeRatio);
				negativeRatioService.save(lastNegativeRatio);
			}
			if (isNegativeIncrease) {
				notificationService.sendListCommentNotification(listCommentNegative, keyword);
			}
			this.sleep(1000 * 60 * 1);
			List<Post> listPost = postService.getIncreasePost(keyword);
			CheckMeaningIncreasePostService CheckMeaningIncreasePostThread = new CheckMeaningIncreasePostService();
			CheckMeaningIncreasePostThread.setData(client, keyword, listPost, listCrisis);
			CheckMeaningIncreasePostThread.start();
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
