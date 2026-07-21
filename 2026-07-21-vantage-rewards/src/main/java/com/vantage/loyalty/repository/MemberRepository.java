package com.vantage.loyalty.repository;

import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.domain.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberNumber(String memberNumber);

    boolean existsByMemberNumber(String memberNumber);

    List<Member> findByStatus(MemberStatus status);
}
