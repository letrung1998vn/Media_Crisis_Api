package fpt.capstone.betatest.services;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.repositories.CrisisRepository;

@Service
public class CrisisService {
	@Autowired
	private CrisisRepository crisisRepository;

	@Autowired
	private PostService postService;

	@Autowired
	private CommentService commentService;

	@Transactional
	public Crisis saveCrisis(Crisis crisis) {
		return crisisRepository.save(crisis);
	}

	@Transactional
	public Crisis findCrisis(BigInteger ContentId, String Type, String keyword, String detectType) {
		return crisisRepository.findByContentIdAndTypeAndKeywordAndDetectType(ContentId, Type, keyword, detectType);
	}

	@Transactional
	public List<Crisis> getCrisisByKeyword(String keyword) {
		return crisisRepository.findByKeyword(keyword);
	}

	@Transactional
	public Crisis getCrisisById(int id) {
		return crisisRepository.findById(id);
	}

	@Transactional
	public void classifyCrisisType(List<Post> listPost, List<Comment> listComment, Crisis crisis) {
		if (crisis.getType().trim().equals("post")) {
			List<Post> post = postService.getPostByPostId(crisis.getContentId());
			if (post != null) {
				listPost.add(post.get(0));
			}
		} else if (crisis.getType().trim().equals("comment")) {
			List<Comment> comment = commentService.getCommentByCommentId(crisis.getContentId());
			if (comment != null) {
				listComment.add(comment.get(0));
			}
		}
	}

	@Transactional
	public boolean containCrisis(List<Crisis> listCrisis, Crisis crisis) {
		boolean result = false;
		for (int i = 0; i < listCrisis.size(); i++) {
			Crisis crisisInList = listCrisis.get(i);
			if (crisisInList.getContentId() == crisis.getContentId()
					&& crisisInList.getKeyword().equals(crisis.getKeyword())
					&& crisisInList.getType().equals(crisis.getType())
					&& crisisInList.getDetectType().equals(crisis.getDetectType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Transactional
	public List<Crisis> updateCrisisPercentage(List<Crisis> listCrisis, Crisis crisis) {
		for (int i = 0; i < listCrisis.size(); i++) {
			Crisis crisisInList = listCrisis.get(i);
			if (crisisInList.getContentId() == crisis.getContentId()
					&& crisisInList.getKeyword().equals(crisis.getKeyword())
					&& crisisInList.getType().equals(crisis.getType())
					&& crisisInList.getDetectType().equals(crisis.getDetectType())) {
				if (crisisInList.getPercentage() < crisis.getPercentage()) {
					crisisInList.setPercentage(crisis.getPercentage());
				}
			}
		}
		return listCrisis;
	}

	@Transactional
	public List<Crisis> insertPostCrisis(Post post, String keyword, String type, List<Crisis> listCrisis,
			String detectType, double percentage) {
		Crisis result = this.findCrisis(post.getPostId(), "post", keyword, detectType);
		if (result == null) {
			Crisis crisis = new Crisis();
			crisis.setContentId(post.getPostId());
			crisis.setType(type);
			crisis.setKeyword(keyword);
			crisis.setDetectType(detectType);
			crisis.setPercentage(percentage);
			if (!containCrisis(listCrisis, crisis)) {
				listCrisis.add(crisis);
			}
			this.saveCrisis(crisis);
		} else {
			if (result.getPercentage() < percentage) {
				result.setPercentage(percentage);
				result = this.saveCrisis(result);
			}
			if (!containCrisis(listCrisis, result)) {
				listCrisis.add(result);
			} else {
				listCrisis = this.updateCrisisPercentage(listCrisis, result);
			}
		}
		return listCrisis;
	}

	@Transactional
	public List<Crisis> insertCommentCrisis(Comment comment, String keyword, List<Crisis> listCrisis, String type,
			String detectType, double percentage) {
		Crisis result = this.findCrisis(comment.getCommentId(), "comment", keyword, detectType);
		if (result == null) {
			Crisis crisis = new Crisis();
			crisis.setContentId(comment.getCommentId());
			crisis.setType(type);
			crisis.setKeyword(keyword);
			crisis.setDetectType(detectType);
			crisis.setPercentage(percentage);
			if (!containCrisis(listCrisis, crisis)) {
				listCrisis.add(crisis);
			}
			this.saveCrisis(crisis);
		} else {
			if (result.getPercentage() < percentage) {
				result.setPercentage(percentage);
				result = this.saveCrisis(result);
			}
			if (!containCrisis(listCrisis, result)) {
				listCrisis.add(result);
			} else {
				listCrisis = this.updateCrisisPercentage(listCrisis, result);
			}
		}
		return listCrisis;
	}

	@Transactional
	public void classifyCrisisType(Crisis crisis, List<Post> listPost, List<Comment> listComment) {
		if (crisis.getType().trim().equals("post")) {
			List<Post> post = postService.getPostByPostId(crisis.getContentId());
			if (post != null && post.size() > 0) {
				listPost.add(post.get(0));
			}
		} else if (crisis.getType().trim().equals("comment")) {
			List<Comment> comment = commentService.getCommentByCommentId(crisis.getContentId());
			if (comment != null && comment.size() > 0) {
				listComment.add(comment.get(0));
			}
		}
	}

	@Transactional
	public double getStandardTimes(double percentage_of_crisis) {
		double result = 0;
		if (percentage_of_crisis <= 93.3) {
			result = 1.5;
		} else if (percentage_of_crisis <= 97.7) {
			result = 2;
		} else if (percentage_of_crisis <= 99.4) {
			result = 2.5;
		} else if (percentage_of_crisis <= 99.9) {
			result = 3;
		}
		return result;
	}

	@Transactional
	public double getPercentage(double std) {
		double result = 0;
		if (std <= 1.5) {
			result = 93.3;
		} else if (std <= 2) {
			result = 97.7;
		} else if (std <= 2.5) {
			result = 99.4;
		} else if (std <= 3) {
			result = 99.9;
		}
		return result;
	}
}
