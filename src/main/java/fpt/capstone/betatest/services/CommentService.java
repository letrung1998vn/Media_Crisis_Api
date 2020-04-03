package fpt.capstone.betatest.services;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Comment;
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
	public List<Comment> getCommentById(BigInteger id) {
		return commentRepository.findByCommentId(id);
	}
}
