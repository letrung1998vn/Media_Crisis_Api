package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Post;

@Repository("postRepository")
public interface PostRepository extends JpaRepository<Post, Integer> {
	@Query(nativeQuery = true, value = "select * from Post s1 "
			+ "JOIN ( select post_content, MAX(crawl_date) as date from Post GROUP BY post_content) s2 "
			+ "on s1.post_content=s2.post_content " + "and s1.crawl_date=s2.date where keyword=?1")
	List<Post> getEachPostContentWithLatestDate(String keyword);

	@Query(nativeQuery = true, value = "select * from Post s1 "
			+ "JOIN ( select post_content, MAX(crawl_date) as date from Post GROUP BY post_content) s2 "
			+ "on s1.post_content=s2.post_content where keyword=?1")
	List<Post> getPostContentWithTwoLatestDate(String keyword);
}
