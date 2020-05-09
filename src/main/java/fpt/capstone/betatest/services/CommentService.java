package fpt.capstone.betatest.services;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.repositories.CommentRepository;
@Service
public class CommentService {
	@Autowired
	private CommentRepository commentRepository;
	@Transactional
	public List<Comment> getCommentByPostId(String postId) {
		return commentRepository.findByPostId(postId);
	}
	
	@Transactional
	public List<Comment> getCommentByCommentId(BigInteger id) {
		return commentRepository.findByCommentId(id);
	}
	@Transactional
	public Comment findCommentByCommentId(String id) {
		return commentRepository.findByCommentId(id);
	}
	@Transactional
	public List<Comment> getCommentByCommentIdSortCrawlDate(BigInteger id) {
		return commentRepository.findByCommentIdOrderByCrawlDateDesc(id);
	}
	@Transactional
	public Comment getCommentById(String id) {
		return commentRepository.findById(id);
	}
	
	@Transactional
	public int findComment(List<Comment> listComment, Comment comment) {
		for (int i = 0; i < listComment.size(); i++) {
			if (comment.getCommentContent().equals(listComment.get(i).getCommentContent())) {
				return i;
			}
		}
		return -1;
	}
	@Transactional
	public Comment save(Comment comment) {
		return commentRepository.save(comment);
	}
	
	@Transactional
	public Page<Comment> getAllNewComment(int Page) {
		Pageable page = PageRequest.of((Page - 1), 20);
		return commentRepository.findAllByOrderByCrawlDateDesc(page);
	}

	@Transactional
	public Page<Comment> getAllNegativeComment(boolean isNegative, int Page) {
		Pageable page = PageRequest.of((Page - 1), 20);
		return commentRepository.findByIsNegativeOrderByCrawlDateDesc(isNegative, page);
	}
}
