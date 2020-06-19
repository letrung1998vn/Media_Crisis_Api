package fpt.capstone.betatest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;

import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.services.KeywordService;
import fpt.capstone.betatest.services.PostService;

@RestController
@RequestMapping("/post")
public class PostController {
	@Autowired
	KeywordService keywordService;
	@Autowired
	PostService postService;
	
	@PostMapping("getNewPost")
	@JsonFormat(timezone = "Asia/Saigon", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	public Page<Post> getNewPost(@RequestParam(value = "page") String page) {
		return postService.getAllNewPost(true, Integer.parseInt(page));
	}
	
	@PostMapping("getNegativePost")
	public Page<Post> getNegativePost(@RequestParam(value = "page") String page) {
		return postService.getAllNegativePost(true, Integer.parseInt(page));
	}
	@PostMapping("getNegativePostByKeyword")
	public List<Post> getNegativePostByKeyword(@RequestParam(value = "keyword") String keyword) {
		return postService.getNegativePostByKeyword(keyword);
	}
}
