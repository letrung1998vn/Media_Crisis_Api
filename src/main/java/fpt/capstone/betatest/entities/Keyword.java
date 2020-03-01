package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the Keyword database table.
 * 
 */
@Entity
@Table(name = "[Keyword]")
public class Keyword implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;

	@Column(name = "Keyword")
	private String keyword;

	// bi-directional many-to-one association to UserInfo
//	@ManyToOne
//	@JoinColumn(name="UserId")
//	private UserInfo userInfo;
	@Column(name = "UserId")
	private String userId;

	@Column(name = "Available")
	private boolean available;
	
	@Column(name = "Version")
	private int version;
//	//bi-directional many-to-one association to Post
//	@OneToMany(mappedBy="keywordBean")
//	private List<Post> posts;

	public Keyword() {
	}

	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

//	public UserInfo getUserInfo() {
//		return this.userInfo;
//	}
//
//	public void setUserInfo(UserInfo userInfo) {
//		this.userInfo = userInfo;
//	}
	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "Keyword [id=" + id + ", keyword=" + keyword + ", userId=" + userId + ", available=" + available
				+ ", version=" + version + "]";
	}

	

//	public List<Post> getPosts() {
//		return this.posts;
//	}
//
//	public void setPosts(List<Post> posts) {
//		this.posts = posts;
//	}

//	public Post addPost(Post post) {
//		getPosts().add(post);
//		post.setKeywordBean(this);
//
//		return post;
//	}
//
//	public Post removePost(Post post) {
//		getPosts().remove(post);
//		post.setKeywordBean(null);
//
//		return post;
//	}

}