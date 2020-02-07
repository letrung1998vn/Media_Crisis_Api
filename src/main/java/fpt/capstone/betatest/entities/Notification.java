package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the Notification database table.
 * 
 */
@Entity
@Table(name="[Notification]")
public class Notification implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID")
	private int id;

	@Column(name="Content")
	private String content;

	//bi-directional many-to-one association to UserInfo
	@ManyToOne
	@JoinColumn(name="UserId")
	private UserInfo userInfo;

	public Notification() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public UserInfo getUserInfo() {
		return this.userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

}