package fpt.capstone.betatest.model;

import java.util.Date;

public class CrisisModel {
	private int id;
    private String type;
    private String DetectType;
    private String Content;
    private Double percentage;
    private String keyword;
    private Date detectDate;
    
    public CrisisModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetectType() {
        return DetectType;
    }

    public void setDetectType(String detectType) {
        DetectType = detectType;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Date getDetectDate() {
		return detectDate;
	}

	public void setDetectDate(Date detectDate) {
		this.detectDate = detectDate;
	}

    
}
