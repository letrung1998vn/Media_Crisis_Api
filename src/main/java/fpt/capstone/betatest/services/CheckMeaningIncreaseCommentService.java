package fpt.capstone.betatest.services;

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
import fpt.capstone.betatest.model.BaseThread;
import fpt.capstone.betatest.services.CrisisService;
import fpt.capstone.betatest.services.LastStandardService;

@Service
public class CheckMeaningIncreaseCommentService extends BaseThread {

	@Autowired
	private LastStandardService lastStandardService;

	@Autowired
	private CrisisService crisisService;

	@Autowired
	private CheckMeaningService checkMeaningService;

	@Autowired
	private NotificationService notificationService;

	public void setData(TextAPIClient client, String keyword, List<Comment> listComment, List<Crisis> listCrisis) {
		this.client = client;
		this.keyword = keyword;
		this.listComment = listComment;
		this.listCrisis = listCrisis;
	}

	@Override
	public synchronized void start() {
		boolean interruptFlag = false;
		if (listComment.size() <= 2) {
			interruptFlag = true;
			if (listCrisis.size() > 0) {
				notificationService.sendNotification(listCrisis, keyword);
			}
		}
		if (!interruptFlag) {
			LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "increaseComment",
					"react");
			LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "increaseComment",
					"comment");
			if (lastCommentStandardReact != null && lastCommentStandardComment != null) {
				int size = 0;
				double reactArray[] = new double[listComment.size() / 2];
				double commentArray[] = new double[listComment.size() / 2];
				double react_upper_limit = 0, comment_upper_limit = 0;
				int x = 0;
				for (int i = 0; i < listComment.size(); i = i + 2) {
					Comment lastComment = listComment.get(i);
					Comment newComment = listComment.get(i + 1);
					reactArray[x] = newComment.getNumberOfReact() - lastComment.getNumberOfReact();
					commentArray[x] = newComment.getNumberOfReply() - lastComment.getNumberOfReply();
					x++;
				}
				if (lastCommentStandardReact != null) {
					react_upper_limit = lastStandardService.calUpperLimit(lastCommentStandardReact.getLastStandard(),
							lastCommentStandardReact.getLastMean());
				}
				if (lastCommentStandardComment != null) {
					comment_upper_limit = lastStandardService.calUpperLimit(
							lastCommentStandardComment.getLastStandard(), lastCommentStandardComment.getLastMean());
				}
				try {
					long startMillis = System.currentTimeMillis();
					Date startDate = new Date(startMillis);
					for (int i = 0; i < listComment.size(); i += 2) {
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
						Comment lastComment = listComment.get(i);
						Comment newComment = listComment.get(i + 1);
						if (lastComment.isNegative() == null) {
							lastComment = checkMeaningService.updateMeaningComment(lastComment, client, keyword);
							countHit += entity_sentiment_count;
						}
						if (newComment.isNegative() == null) {
							newComment = checkMeaningService.updateMeaningComment(newComment, client, keyword);
							countHit += entity_sentiment_count;
						}
						if (lastComment.isNegative() && newComment.isNegative()) {
							if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) > comment_upper_limit
									|| (lastComment.getNumberOfReact()
											- newComment.getNumberOfReact()) > react_upper_limit) {
								// Add Crisis to Db
								listCrisis = crisisService.insertCommentCrisis(newComment, keyword, listCrisis,
										commentType);
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
}
