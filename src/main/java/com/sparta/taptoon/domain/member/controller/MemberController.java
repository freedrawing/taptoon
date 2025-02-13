package com.sparta.taptoon.domain.member.controller;

import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.entity.MemberDetail;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import com.sparta.taptoon.domain.member.service.MemberService;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/password")
    public void updatePassword(@AuthenticationPrincipal MemberDetail memberDetail, @RequestBody String password) {
        memberService.changeUserPassword(memberDetail.getId(), password);
        ApiResponse.noContent();
    }

    @PatchMapping("/nickname")
    public void updateNickname(@AuthenticationPrincipal MemberDetail memberDetail, @RequestParam String nickname) {
        memberService.changeUserNickname(memberDetail.getId(), nickname);
        ApiResponse.noContent();
    }

    @PatchMapping("/grade")
    public void updateGrade(@AuthenticationPrincipal MemberDetail memberDetail, @RequestParam MemberGrade grade) {
        memberService.changeUserGrade(memberDetail.getId(), grade);
        ApiResponse.noContent();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MemberResponse>> getUserInfo(@AuthenticationPrincipal MemberDetail memberDetail) {
        MemberResponse memberResponse = memberService.findMember(memberDetail.getId());
        return ApiResponse.success(memberResponse);
    }

    @DeleteMapping
    public void deleteMember(@AuthenticationPrincipal MemberDetail memberDetail) {
        memberService.removeMember(memberDetail.getId());
    }
}
