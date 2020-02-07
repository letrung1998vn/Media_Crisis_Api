package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the Keyword database table.
 * 
 */
@Entity
@Table(name="[Keyword]")
public class Keyword implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="Keyword")
	private String keyword;

	//bi-directional many-to-one association to UserInfo
	@ManyToOne
	@JoinColumn(name="UserId")
	private UserInfo userInfo;

	//bi-directional many-to-one association to Post
	@OneToMany(mappedBy="keywordBean")
	private List<Post> posts;

	public Keyword() {
	}

	public String getKeyword() {
		return this.keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public UserInfo getUserInfo() {
		return this.userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public List<Post> getPosts() {
		return this.posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public Post addPost(Post post) {
		getPosts().add(post);
		post.setKeywordBean(this);

		return post;
	}

	public Post removePost(Post post) {
		getPosts().remove(post);
		post.setKeywordBean(null);

		return post;
	}

}