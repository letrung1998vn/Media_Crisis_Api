package fpt.capstone.betatest.entities;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Crisis")
public class Crisis {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "content_id")
	private BigInteger contentId;
	
	@Column(name="type")
	private String type;
	
	@Column(name = "keyword")
	private String keyword;
	
	@Column(name = "detect_type")
	private String detectType;
	
	@Column(name = "percentage")
	private double percentage;
	
	@Column(name = "detect_date")
	private Date detectDate;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigInteger getContentId() {
		return contentId;
	}

	public void setContentId(BigInteger contentId) {
		this.contentId = contentId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getDetectType() {
		return detectType;
	}

	public void setDetectType(String detectType) {
		this.detectType = detectType;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	public Date getDetectDate() {
		return detectDate;
	}

	public void setDetectDate(Date detectDate) {
		this.detectDate = detectDate;
	}
	
}
