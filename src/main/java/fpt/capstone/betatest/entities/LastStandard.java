package fpt.capstone.betatest.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Last_Standard")
public class LastStandard {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "keyword")
	private String keyword;

	@Column(name = "type")
	private String type;

	@Column(name = "last_standard")
	private float lastStandard;

	@Column(name = "last_mean")
	private float lastMean;

	@Column(name = "last_number")
	private int lastNumber;

	@Column(name = "number_type")
	private String numberType;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getLastStandard() {
		return lastStandard;
	}

	public void setLastStandard(float lastStandard) {
		this.lastStandard = lastStandard;
	}

	public float getLastMean() {
		return lastMean;
	}

	public void setLastMean(float lastMean) {
		this.lastMean = lastMean;
	}

	public int getLastNumber() {
		return lastNumber;
	}

	public void setLastNumber(int lastNumber) {
		this.lastNumber = lastNumber;
	}

	public String getNumberType() {
		return numberType;
	}

	public void setNumberType(String numberType) {
		this.numberType = numberType;
	}

}
