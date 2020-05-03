package fpt.capstone.betatest.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.model.BaseThread;

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

	public void setData(StanfordCoreNLP pipeline, String keyword, List<Comment> listComment, List<Crisis> listCrisis) {
		this.pipeline = pipeline;
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
					detectTypeReact);
			LastStandard lastCommentStandardComment = lastStandardService.getLastStandard(keyword, "increaseComment",
					detectTypeComment);
			if (lastCommentStandardReact != null && lastCommentStandardComment != null) {
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
					for (int i = 0; i < listComment.size(); i += 2) {
						Comment lastComment = listComment.get(i);
						Comment newComment = listComment.get(i + 1);
						if (lastComment.isNegative() == null) {
							lastComment = checkMeaningService.updateMeaningComment(lastComment, pipeline, keyword);
						}
						if (newComment.isNegative() == null) {
							newComment = checkMeaningService.updateMeaningComment(newComment, pipeline, keyword);
						}
						if (lastComment.isNegative() && newComment.isNegative()) {
							if ((lastComment.getNumberOfReply() - newComment.getNumberOfReply()) > comment_upper_limit) {
								// Add Crisis to Db
								System.out.println("Crisis comment increase: "+ newComment.getCommentId());
								listCrisis = crisisService.insertCommentCrisis(newComment, keyword, listCrisis,
										commentType,detectTypeIncreaseReact);
							} else if((lastComment.getNumberOfReact()
											- newComment.getNumberOfReact()) > react_upper_limit) {
								System.out.println("Crisis comment increase: "+ newComment.getCommentId());
								listCrisis = crisisService.insertCommentCrisis(newComment, keyword, listCrisis,
										commentType,detectTypeIncreasComment);
							}
						}
					}
					this.interrupt();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
