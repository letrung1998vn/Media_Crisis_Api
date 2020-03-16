package fpt.capstone.betatest.repositories;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.capstone.betatest.entities.Crisis;
import fpt.capstone.betatest.entities.Post;
import fpt.capstone.betatest.entities.User;

@Repository("crisisRepository")
public interface CrisisRepository extends JpaRepository<Crisis, Integer>{
	Crisis findByContentIdAndType(BigInteger ContentId,String Type);
}
