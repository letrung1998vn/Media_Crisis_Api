package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.User;


@Repository("usersRepository")
public interface UserRepository extends JpaRepository<User, Integer> {
	
	User findByUserName(String userName);

	User findByUserNameAndPassword(String username,String password);
	List<User> findAll();
	Page<User> findByUserNameContaining(String username, Pageable pageable);

}
