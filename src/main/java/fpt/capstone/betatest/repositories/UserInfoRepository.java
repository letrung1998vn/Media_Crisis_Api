package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.User;
import fpt.capstone.betatest.entities.UserInfo;


@Repository("usersInfoRepository")
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
//	List<UserInfo> findAll();
//	UserInfo findByUser(User user);
//	Page<UserInfo> findByUserIdContaining(String userId, Pageable pageable);
//	
	UserInfo findByUserId(String userId);
}
