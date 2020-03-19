package fpt.capstone.betatest.repositories;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Comment;
import fpt.capstone.betatest.entities.Post;

@Repository("commentRepository")
public interface CommentRepository extends JpaRepository<Comment, Integer> {
	List<Comment> findByPostId(BigInteger postId);
	
	Comment findById(BigInteger id);
}
