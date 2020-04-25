package fpt.capstone.betatest.services;

import java.util.List;

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
import fpt.capstone.betatest.model.BaseThread;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.LastStandardService;

@Service
public class CheckMeaningIncreaseCommentService extends BaseThread{
	
	@Autowired
	private LastStandardService lastStandardService;

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
				react_upper_limit = lastStandardService.calUpperLimit(lastCommentStandardReact.getLastStandard(), lastCommentStandardReact.getLastMean());
			}
			if (lastCommentStandardComment != null) {
				comment_upper_limit = lastStandardService.calUpperLimit(lastCommentStandardComment.getLastStandard(), lastCommentStandardComment.getLastMean());
			}
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
								if ((lastComment.getNumberOfReply()
										- newComment.getNumberOfReply()) > comment_upper_limit
										|| (lastComment.getNumberOfReact()
												- newComment.getNumberOfReact()) > react_upper_limit) {
									// Add Crisis to Db
									crisisService.insertCommentCrisis(newComment, word, listCrisis, word);;
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
							if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) > comment_upper_limit
									|| (lastComment.getNumberOfReact()
											- newComment.getNumberOfReact()) > react_upper_limit) {
								// Add Crisis to Db
								crisisService.insertCommentCrisis(newComment, keyword, listCrisis, type);;
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
}
