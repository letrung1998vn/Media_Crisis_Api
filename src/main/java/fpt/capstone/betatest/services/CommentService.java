package fpt.capstone.betatest.services;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.repositories.CommentRepository;
import fpt.capstone.betatest.repositories.CrisisRepository;
@Service
public class CommentService {
	@Autowired
	private CommentRepository commentRepository;
	@Transactional
	public List<Comment> getCommentByPostId(BigInteger postId) {
		return commentRepository.findByPostId(postId);
	}
}
