package fpt.capstone.betatest.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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

	public void setData(StanfordCoreNLP pipeline, String keyword, List<Post> listPost, List<Crisis> listCrisis) {
		this.pipeline = pipeline;
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
					for (int i = 0; i < listPost.size(); i = i + 2) {
						Post post = listPost.get(i);
						Post nextPost = listPost.get(i + 1);
						if (post.isNegative() == null) {
							post = checkMeaningService.updateMeaningPost(post, pipeline, keyword);
						}
						if (nextPost.isNegative() == null) {
							nextPost = checkMeaningService.updateMeaningPost(nextPost, pipeline, keyword);
						}
						if (post.isNegative() && nextPost.isNegative()) {
							if ((post.getNumberOfReply() - nextPost.getNumberOfReply()) > comment_upper_limit) {
								// Add Crisis To Db
								System.out.println("Crisis post increase: " + nextPost.getPostId());
								listCrisis = crisisService.insertPostCrisis(nextPost, keyword, postType, listCrisis,
										detectTypeIncreaseReact);
							} else if ((post.getNumberOfReweet() - nextPost.getNumberOfReweet()) > share_upper_limit) {
								System.out.println("Crisis post increase: " + nextPost.getPostId());
								listCrisis = crisisService.insertPostCrisis(nextPost, keyword, postType, listCrisis,
										detectTypeIncreaseShare);
							} else if ((post.getNumberOfReact() - nextPost.getNumberOfReact()) > react_upper_limit) {
								System.out.println("Crisis post increase: " + nextPost.getPostId());
								listCrisis = crisisService.insertPostCrisis(nextPost, keyword, postType, listCrisis,
										detectTypeIncreaseReact);
							}
						}
					}
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
					CheckMeaningIncreaseCommentThread.setData(pipeline, keyword, listComment, listCrisis);
					CheckMeaningIncreaseCommentThread.start();
					this.interrupt();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
