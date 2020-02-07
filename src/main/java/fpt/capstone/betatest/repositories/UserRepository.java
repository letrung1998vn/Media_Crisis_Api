package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.User;


@Repository("usersRepository")
public interface UserRepository extends JpaRepository<User, Integer> {
	
	public final static String GET_USER_BY_USERNAME = "SELECT lr FROM User lr WHERE UserName = :username";

	@Query(GET_USER_BY_USERNAME)
	User checkUserexist(@Param("username") String username);
	
	User findByUserNameAndPassword(String username,String password);
	List<User> findAll();
	
}
