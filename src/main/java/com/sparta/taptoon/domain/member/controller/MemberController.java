package com.sparta.taptoon.domain.member.controller;

import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import com.sparta.taptoon.domain.member.service.MemberService;
import com.sparta.taptoon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/password/{id}")
    public void updatePassword(@PathVariable Long id, @RequestBody String password) {
        memberService.changeUserPassword(id, password);
        ApiResponse.noContent();
    }

    @PatchMapping("/nickname/{id}")
    public void updateNickname(@PathVariable Long id, @RequestParam String nickname) {
        memberService.changeUserNickname(id, nickname);
        ApiResponse.noContent();
    }

    @PatchMapping("/grade/{id}")
    public void updateGrade(@PathVariable Long id, @RequestParam MemberGrade grade) {
        memberService.changeUserGrade(id, grade);
        ApiResponse.noContent();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getUserInfo(@PathVariable Long id) {
        MemberResponse memberResponse = memberService.findMember(id);
        return ApiResponse.success(memberResponse);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id) {
        memberService.removeMember(id);
    }
}
