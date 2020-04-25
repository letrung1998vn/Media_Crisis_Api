package fpt.capstone.betatest.repositories;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Comment;

@Repository("commentRepository")
public interface CommentRepository extends JpaRepository<Comment, Integer> {
	List<Comment> findByPostId(String postId);
	
	List<Comment> findByCommentId(BigInteger id);
	Comment findById(String id);
}
