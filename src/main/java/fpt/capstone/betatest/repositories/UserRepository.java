package fpt.capstone.betatest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.UserEntity;


@Repository("usersRepository")
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	UserEntity findByUsernameAndPassword(String username,String password);
	List<UserEntity> findAll();
	
}
