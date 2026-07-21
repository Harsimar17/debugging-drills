package com.vantage.loyalty.service;

import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.dto.EnrollMemberRequest;
import com.vantage.loyalty.dto.MemberDto;
import com.vantage.loyalty.exception.ResourceNotFoundException;
import com.vantage.loyalty.mapper.MemberMapper;
import com.vantage.loyalty.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberService(MemberRepository memberRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    @Transactional
    public MemberDto enroll(EnrollMemberRequest request) {
        String memberNumber = generateMemberNumber();
        Member member = new Member(memberNumber, request.getFullName(), request.getEmail(), request.getTier());
        Member saved = memberRepository.save(member);
        log.info("Enrolled member {} ({}) at tier {}", saved.getMemberNumber(), saved.getEmail(), saved.getTier());
        return memberMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public MemberDto get(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
        return memberMapper.toDto(member);
    }

    private String generateMemberNumber() {
        String candidate;
        do {
            candidate = "VG" + ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
        } while (memberRepository.existsByMemberNumber(candidate));
        return candidate;
    }
}
