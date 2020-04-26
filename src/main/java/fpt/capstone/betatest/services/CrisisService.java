package fpt.capstone.betatest.services;

import java.math.BigInteger;
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
	public void saveCrisis(Crisis crisis) {
		crisisRepository.save(crisis);
	}

	@Transactional
	public Crisis findCrisis(BigInteger ContentId, String Type, String keyword) {
		return crisisRepository.findByContentIdAndTypeAndKeyword(ContentId, Type, keyword);
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
					&& crisisInList.getType().equals(crisis.getType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Transactional
	public List<Crisis> insertPostCrisis(Post post, String keyword, String type, List<Crisis> listCrisis) {
		Crisis result = this.findCrisis(post.getPostId(), "post", keyword);
		if (result == null) {
			Crisis crisis = new Crisis();
			crisis.setContentId(post.getPostId());
			crisis.setType(type);
			crisis.setKeyword(keyword);
			if (!containCrisis(listCrisis, crisis)) {
				listCrisis.add(crisis);
			}
			this.saveCrisis(crisis);
			// send crisis to notification
		} else {
			if (!containCrisis(listCrisis, result)) {
				listCrisis.add(result);
			}
		}
		return listCrisis;
	}

	@Transactional
	public List<Crisis> insertCommentCrisis(Comment comment, String keyword, List<Crisis> listCrisis, String type) {
		Crisis result = this.findCrisis(comment.getCommentId(), "comment", keyword);
		if (result == null) {
			Crisis crisis = new Crisis();
			crisis.setContentId(comment.getCommentId());
			crisis.setType(type);
			crisis.setKeyword(keyword);
			if (!containCrisis(listCrisis, crisis)) {
				listCrisis.add(crisis);
			}
			this.saveCrisis(crisis);
		} else {
			if (!containCrisis(listCrisis, result)) {
				listCrisis.add(result);
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

}
