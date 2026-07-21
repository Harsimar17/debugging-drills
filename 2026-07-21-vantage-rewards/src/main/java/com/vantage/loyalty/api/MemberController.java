package com.vantage.loyalty.api;

import com.vantage.loyalty.dto.EnrollMemberRequest;
import com.vantage.loyalty.dto.MemberDto;
import com.vantage.loyalty.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDto enroll(@Valid @RequestBody EnrollMemberRequest request) {
        return memberService.enroll(request);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto> get(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberService.get(memberId));
    }
}
