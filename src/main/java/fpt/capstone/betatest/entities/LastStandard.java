package fpt.capstone.betatest.entities;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "LastStandard")
public class LastStandard {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "keyword")
	private String keyword;

	@Column(name = "type")
	private String type;

	@Column(name = "last_standard")
	private double lastStandard;

	@Column(name = "last_mean")
	private double lastMean;

	@Column(name = "last_number")
	private double lastNumber;

	@Column(name = "number_type")
	private String numberType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public double getLastStandard() {
		return lastStandard;
	}

	public void setLastStandard(double lastStandard) {
		this.lastStandard = lastStandard;
	}

	public double getLastMean() {
		return lastMean;
	}

	public void setLastMean(double lastMean) {
		this.lastMean = lastMean;
	}

	public double getLastNumber() {
		return lastNumber;
	}

	public void setLastNumber(double lastNumber) {
		this.lastNumber = lastNumber;
	}

	public String getNumberType() {
		return numberType;
	}

	public void setNumberType(String numberType) {
		this.numberType = numberType;
	}

}
