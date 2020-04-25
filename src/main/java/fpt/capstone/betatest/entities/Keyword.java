package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;

//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
	@Column(name = "id")
	private int id;

	@Column(name = "keyword")
	private String keyword;

	// bi-directional many-to-one association to UserInfo
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
//	@Column(name = "user_id")
//	private String userId;

	@Column(name = "available")
	private boolean available;

	@Column(name = "version")
	private int version;
//	//bi-directional many-to-one association to Post
//	@OneToMany(mappedBy="keywordBean")
//	private List<Post> posts;

	public Keyword() {
	}

	public Keyword(int id, String keyword, User user, boolean available, int version) {
		super();
		this.id = id;
		this.keyword = keyword;
		this.user = user;
		this.available = available;
		this.version = version;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

//	public String getUserId() {
//		return userId;
//	}
//
//	public void setUserId(String userId) {
//		this.userId = userId;
//	}
	
	

	public boolean isAvailable() {
		return available;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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



}