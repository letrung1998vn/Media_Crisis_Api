package fpt.capstone.betatest.repositories;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Post;

@Repository("postRepository")
public interface PostRepository extends JpaRepository<Post, Integer> {
	@Query(nativeQuery = true, value = "select * from Post p,"
			+ " (select post_content, MAX(crawl_date) as date from Post GROUP BY post_content) w "
			+ "where p.post_content=w.post_content and p.crawl_date=w.date and keyword=?1")
	List<Post> getEachPostContentWithLatestDate(String keyword);

	@Query(nativeQuery = true, value = "select * from Post where keyword=?1 order by post_content")
	List<Post> getPostContentWithTwoLatestDate(String keyword);
	
	List<Post> getByPostId(BigInteger id);
}
