package fpt.capstone.betatest.model;

import java.util.List;

public class UserCrisis {
	List<CrisisModel> crisisList;
    String keyword;

    public UserCrisis() {

    }

    public List<CrisisModel> getCrisisList() {
        return crisisList;
    }

    public void setCrisisList(List<CrisisModel> crisisList) {
        this.crisisList = crisisList;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return "UserCrisis{" + "crisisList=" + crisisList + ", keyword=" + keyword + '}';
    }
}
