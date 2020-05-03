package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.NegativeRatio;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.BaseThread;

@Service
public class CheckMeaningCurrentCommentService extends BaseThread {

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

	@Autowired
	private CheckMeaningIncreasePostService CheckMeaningIncreasePostThread;

	@Autowired
	private CheckMeaningService checkMeaningService;

	public void setData(StanfordCoreNLP pipeline, String keyword, List<Comment> listComment, List<Crisis> listCrisis) {
		this.pipeline = pipeline;
		this.keyword = keyword;
		this.listComment = listComment;
		this.listCrisis = listCrisis;
	}

	@Override
	public synchronized void start() {
		boolean interruptFlag = false;
		if (listComment.size() <= 0) {
			interruptFlag = true;
			if (listCrisis.size() > 0) {
				notificationService.sendNotification(listCrisis, keyword);
			}
		}
		if (!interruptFlag) {
			List<Comment> listCommentNegative = new ArrayList<>();
			LastStandard lastCommentStandardReact = lastStandardService.getLastStandard(keyword, "comment", "react");
			LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "comment",
					"comment");
			double react_upper_limit = 0;
			if (lastCommentStandardReact != null) {
				react_upper_limit = lastStandardService.calUpperLimit(lastCommentStandardReact.getLastStandard(),
						lastCommentStandardReact.getLastMean());
			}
			double comment_upper_limit = 0;
			if (lastCommentStandardComment != null) {
				comment_upper_limit = lastStandardService.calUpperLimit(lastCommentStandardComment.getLastStandard(),
						lastCommentStandardComment.getLastMean());
			}

			try {
				for (int i = 0; i < listComment.size(); i++) {
					Comment comment = listComment.get(i);
					if (comment.isNegative() == null) {
						checkMeaningService.updateMeaningComment(comment, pipeline, keyword);
					}
					if (comment.isNegative()) {
						listCommentNegative.add(comment);
						if (comment.getNumberOfReply() > comment_upper_limit) {
							System.out.println("Crisis increase comment: " + comment.getCommentId());
							listCrisis = crisisService.insertCommentCrisis(comment, keyword, listCrisis, commentType,
									detectTypeComment);
						} else if (comment.getNumberOfReact() > react_upper_limit) {
							System.out.println("Crisis increase comment: " + comment.getCommentId());
							listCrisis = crisisService.insertCommentCrisis(comment, keyword, listCrisis, commentType,
									detectTypeReact);
						}
					}
				}
				double negativeRatio = (double) listCommentNegative.size() / (double) listComment.size();
				List<NegativeRatio> lastNegativeRatio = negativeRatioService.getNegativeRatio(keyword, commentType);
				System.out.println("Check comment ratio: " + negativeRatio);
				long millis = System.currentTimeMillis();
				Date date = new Date(millis);
				boolean isNegativeIncrease = false;
				if (lastNegativeRatio != null&& lastNegativeRatio.size()>0) {
					if (lastNegativeRatio.get(0).getUpdateDate().before(date)) {
						// long diffInMillies = Math.abs(date.getTime() -
						// lastNegativeRatio.get(0).getUpdateDate().getTime());
						// long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						// if (diff > differenceHour) {
						NegativeRatio newNegativeRatio = new NegativeRatio();
						newNegativeRatio.setKeyword(keyword);
						newNegativeRatio.setType(commentType);
						newNegativeRatio.setRatio(negativeRatio);
						newNegativeRatio.setUpdateDate(date);
						negativeRatioService.save(newNegativeRatio);
						if (lastNegativeRatio.get(0).getRatio() < negativeRatio) {
							if (negativeRatio - lastNegativeRatio.get(0).getRatio() > ratioLimit) {
								isNegativeIncrease = true;
								// }
							}
						}
					}
				} else {
					NegativeRatio newNegativeRatio = new NegativeRatio();
					newNegativeRatio.setKeyword(keyword);
					newNegativeRatio.setType(commentType);
					newNegativeRatio.setRatio(negativeRatio);
					newNegativeRatio.setUpdateDate(date);
					negativeRatioService.save(newNegativeRatio);
				}
				if (isNegativeIncrease) {
					notificationService.sendListCommentNotification(listCommentNegative, keyword);
				}
				List<Post> listPost = postService.getIncreasePost(keyword);
				CheckMeaningIncreasePostThread.setData(pipeline, keyword, listPost, listCrisis);
				CheckMeaningIncreasePostThread.start();
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
