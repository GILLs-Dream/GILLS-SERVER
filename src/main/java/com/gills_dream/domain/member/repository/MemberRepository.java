package com.gills_dream.domain.member.repository;

import com.gills_dream.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Query("select m from Member m where m.loginType = 'APPLE' and m.sub = :sub")
    Optional<Member> findByAppleSub(String sub);

    boolean existsByEmail(String newEmail);
}
