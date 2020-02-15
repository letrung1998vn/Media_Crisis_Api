package fpt.capstone.betatest.model;

public class UserLoginOutput {
String userId, password, email, name, keyword, role;
int keywordId;

public UserLoginOutput() {

}

public UserLoginOutput(String userId, String password, String email, String name, String keyword, int keywordId, String role) {
	super();
	this.userId = userId;
	this.password = password;
	this.email = email;
	this.name = name;
	this.keyword = keyword;
	this.keywordId = keywordId;
	this.role = role;
}

public String getUserId() {
	return userId;
}

public void setUserId(String userId) {
	this.userId = userId;
}

public String getPassword() {
	return password;
}

public void setPassword(String password) {
	this.password = password;
}

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public String getKeyword() {
	return keyword;
}

public void setKeyword(String keyword) {
	this.keyword = keyword;
}

public int getKeywordId() {
	return keywordId;
}

public void setKeywordId(int keywordId) {
	this.keywordId = keywordId;
}

public String getRole() {
	return role;
}

public void setRole(String role) {
	this.role = role;
}

@Override
public String toString() {
	return "UserLoginOutput [userId=" + userId + ", password=" + password + ", email=" + email + ", name=" + name
			+ ", keyword=" + keyword + ", role=" + role + ", keywordId=" + keywordId + "]";
}

}
