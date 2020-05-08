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
import fpt.capstone.betatest.entities.Keyword;
import fpt.capstone.betatest.entities.LastStandard;
import fpt.capstone.betatest.entities.NegativeRatio;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.model.BaseThread;

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

	@Autowired
	private CheckMeaningService checkMeaningService;

	@Autowired
	private KeywordService keywordService;

	public void setData(StanfordCoreNLP pipeline, String keyword, List<Post> listPost, List<Crisis> listCrisis) {
		this.pipeline = pipeline;
		this.keyword = keyword;
		this.listPost = listPost;
		this.listCrisis = listCrisis;
	}

	@Override
	public synchronized void start() {
		boolean interruptFlag = false;
		if (listPost.size() <= 0) {
			interruptFlag = true;
			if (listCrisis.size() > 0) {
				notificationService.sendNotification(listCrisis, keyword);
			}
		}
		if (!interruptFlag) {

			try {
				LastStandard lastPostStandardReact = lastStandardService.getLastStandard(keyword, "post", "react");
				LastStandard lastPostStandardShare = lastStandardService.getLastStandard(keyword, "post", "share");
				LastStandard lastPostStandardComment = lastStandardService.getLastStandard(keyword, "post", "comment");
				List<Keyword> listKey = keywordService.getUserByKeyword(keyword);
				List<Post> listPostNegative = new ArrayList<>();
				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					if (post.isNegative() == null) {
						post = checkMeaningService.updateMeaningPost(post, pipeline, keyword);
					}
					if (post.isNegative()) {
						listPostNegative.add(post);
						for (int x = 0; x < listKey.size(); x++) {
							Keyword keywordObj = listKey.get(x);
							double std = crisisService.getStandardTimes(keywordObj.getPercent_of_crisis());
							double react_upper_limit = 0;
							if (lastPostStandardReact != null) {
								react_upper_limit = lastStandardService.calUpperLimit(
										lastPostStandardReact.getLastStandard(), lastPostStandardReact.getLastMean(),
										std);
							}
							double share_upper_limit = 0;
							if (lastPostStandardShare != null) {
								share_upper_limit = lastStandardService.calUpperLimit(
										lastPostStandardShare.getLastStandard(), lastPostStandardShare.getLastMean(),
										std);
							}
							double comment_upper_limit = 0;
							if (lastPostStandardComment != null) {
								comment_upper_limit = lastStandardService.calUpperLimit(
										lastPostStandardComment.getLastStandard(),
										lastPostStandardComment.getLastMean(), std);
							}
							double percentage = crisisService.getPercentage(std);

							if (post.getNumberOfReply() > comment_upper_limit) {
								// Save crisis and check if already add or not
								System.out.println("Crisis post: " + post.getPostId());
								listCrisis = crisisService.insertPostCrisis(post, keyword, postType, listCrisis,
										detectTypeComment, percentage);
							} else if (post.getNumberOfReweet() > share_upper_limit) {
								System.out.println("Crisis post: " + post.getPostId());
								listCrisis = crisisService.insertPostCrisis(post, keyword, postType, listCrisis,
										detectTypeShare, percentage);
							} else if (post.getNumberOfReact() > react_upper_limit) {
								System.out.println("Crisis post: " + post.getPostId());
								listCrisis = crisisService.insertPostCrisis(post, keyword, postType, listCrisis,
										detectTypeReact, percentage);
							}
						}

					}
				}
				double negativeRatio = negativeRatioService.calNegativeRatio(listPost.size(), listPostNegative.size());
				List<NegativeRatio> lastNegativeRatio = negativeRatioService.getNegativeRatio(keyword, postType);
				System.out.println("Check post ratio: " + negativeRatio);
				long millis = System.currentTimeMillis();
				Date date = new Date(millis);
				boolean isNegativeIncrease = false;
				if (lastNegativeRatio != null && lastNegativeRatio.size() > 0) {
					if (lastNegativeRatio.get(0).getUpdateDate().before(date)) {
						long diffInMillies = Math
								.abs(date.getTime() - lastNegativeRatio.get(0).getUpdateDate().getTime());
						long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						if (diff >= differenceHour) {
							if (lastNegativeRatio.get(0).getRatio() < negativeRatio) {
								NegativeRatio newNegativeRatio = new NegativeRatio();
								newNegativeRatio.setKeyword(keyword);
								newNegativeRatio.setType(postType);
								newNegativeRatio.setRatio(negativeRatio);
								newNegativeRatio.setUpdateDate(date);
								negativeRatioService.save(newNegativeRatio);
								if (negativeRatio - lastNegativeRatio.get(0).getRatio() > ratioLimit) {
									isNegativeIncrease = true;
								}
							} else {
								NegativeRatio newNegativeRatio = new NegativeRatio();
								newNegativeRatio.setKeyword(keyword);
								newNegativeRatio.setType(postType);
								newNegativeRatio.setRatio(negativeRatio);
								newNegativeRatio.setUpdateDate(date);
								negativeRatioService.save(newNegativeRatio);
							}
						}
					}
				} else {
					NegativeRatio newNegativeRatio = new NegativeRatio();
					newNegativeRatio.setKeyword(keyword);
					newNegativeRatio.setType(postType);
					newNegativeRatio.setRatio(negativeRatio);
					newNegativeRatio.setUpdateDate(date);
					negativeRatioService.save(newNegativeRatio);
				}
				if (isNegativeIncrease) {
					notificationService.sendListPostNotification(listPostNegative, keyword);
				}
				List<Comment> listComment = new ArrayList<>();
				for (int i = 0; i < listPost.size(); i++) {
					Post post = listPost.get(i);
					listComment.addAll(commentService.getCommentByPostId(post.getId()));
				}
				CheckMeaningCurrentCommentThread.setData(pipeline, keyword, listComment, listCrisis);
				CheckMeaningCurrentCommentThread.start();
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
