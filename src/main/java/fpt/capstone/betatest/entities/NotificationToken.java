package fpt.capstone.betatest.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "NotiToken")
public class NotificationToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "notiToken")
	private String notiToken;

	@Column(name = "user_id")
	private String userName;
	
	@Column(name = "available")
	private boolean available;
	
	@Column(name = "activeTime")
	private Date activeTime;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNotiToken() {
		return notiToken;
	}

	public void setNotiToken(String notiToken) {
		this.notiToken = notiToken;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public Date getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(Date activeTime) {
		this.activeTime = activeTime;
	}

	@Override
	public String toString() {
		return "NotificationToken [id=" + id + ", notiToken=" + notiToken + ", userName=" + userName + ", available="
				+ available + ", activeTime=" + activeTime + "]";
	}
	
	
}
