package com.vantage.loyalty.mapper;

import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.dto.MemberDto;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberDto toDto(Member member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setMemberNumber(member.getMemberNumber());
        dto.setFullName(member.getFullName());
        dto.setEmail(member.getEmail());
        dto.setTier(member.getTier());
        dto.setStatus(member.getStatus());
        dto.setEnrolledAt(member.getEnrolledAt());
        return dto;
    }
}
