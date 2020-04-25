package fpt.capstone.betatest.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the UserInfo database table.
 * 
 */
@Entity
@Table(name = "[UserInfo]")
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String email;

	@Column(name = "link_webhook")
	private String link_webhook;

	@Column(name = "version")
	private int version;

	public UserInfo() {
	}

	public UserInfo(String userId, String email, String name, int version) {
		super();
		this.userId = userId;
		this.email = email;
		this.name = name;
		this.version = version;
	}

	public UserInfo(String userId, String name, String email, String link_webhook, int version) {
		super();
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.link_webhook = link_webhook;
		this.version = version;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getLink_webhook() {
		return link_webhook;
	}

	public void setLink_webhook(String link_webhook) {
		this.link_webhook = link_webhook;
	}

}