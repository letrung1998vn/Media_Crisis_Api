package fpt.capstone.betatest.services;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.repositories.PostRepository;

@Service
public class PostService {
	@Autowired
	private PostRepository postRepository;
	@Transactional
	public List<Post> getEachPostContentWithLatestDate(String keyword) {
		return postRepository.getEachPostContentWithLatestDate(keyword);
	}
	@Transactional
	public List<Post> getPostContentWithTwoLatestDate(String keyword) {
		return postRepository.getPostContentWithTwoLatestDate(keyword);
	}
	
	@Transactional
	public Post getPostById(BigInteger id) {
		return postRepository.getById(id);
	}
}
