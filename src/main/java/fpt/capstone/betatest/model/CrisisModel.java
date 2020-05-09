package fpt.capstone.betatest.model;

public class CrisisModel {
	private int id;
    private String type;
    private String DetectType;
    private String Content;
    private Double percentage;

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

    @Override
    public String toString() {
        return "Crisis{" + "id=" + id + ", type=" + type + ", DetectType=" + DetectType + ", Content=" + Content + ", percentage=" + percentage + '}';
    }
}
