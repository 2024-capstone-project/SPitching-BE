package djj.spitching_be.Repository;

import djj.spitching_be.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>{
    Optional<User> findByEmail(String email); // 중복 가입 확인
}