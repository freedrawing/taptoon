package com.sparta.taptoon.domain.member.controller;

import com.sparta.taptoon.domain.member.dto.response.MemberResponse;
import com.sparta.taptoon.domain.member.dto.MemberDetail;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import com.sparta.taptoon.domain.member.service.MemberService;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member", description = "사용자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "이메일 설정")
    @PatchMapping("/email")
    public ResponseEntity<ApiResponse<Void>> setEmail(@AuthenticationPrincipal MemberDetail memberDetail, @RequestBody String email) {
        memberService.setMemberEmail(memberDetail.getMember(), email);
        return ApiResponse.noContent();
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@AuthenticationPrincipal MemberDetail memberDetail, @RequestBody String password) {
        memberService.changeUserPassword(memberDetail.getMember(), password);
        return ApiResponse.noContent();
    }

    @Operation(summary = "닉네임 변경")
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<Void>> updateNickname(@AuthenticationPrincipal MemberDetail memberDetail, @RequestParam String nickname) {
        memberService.changeUserNickname(memberDetail.getMember(), nickname);
        return ApiResponse.noContent();
    }

    @Operation(summary = "등급 변경")
    @PatchMapping("/grade")
    public ResponseEntity<ApiResponse<Void>> updateGrade(@AuthenticationPrincipal MemberDetail memberDetail, @RequestParam MemberGrade grade) {
        memberService.changeUserGrade(memberDetail.getMember(), grade);
        return ApiResponse.noContent();
    }

    @Operation(summary = "멤버 정보 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<MemberResponse>> getUserInfo(@AuthenticationPrincipal MemberDetail memberDetail) {
        MemberResponse memberResponse = memberService.findMember(memberDetail.getMember());
        return ApiResponse.success(memberResponse);
    }

    @Operation(summary = "이름이나 닉네임으로 멤버 정보 조회")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getUsers(@RequestParam(required = false) String name,
                                                                      @RequestParam(required = false) String nickname,
                                                                      @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MemberResponse> memberResponses = memberService.findMemberByNameOrNickname(name,nickname, pageable);
        return ApiResponse.success(memberResponses);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping
    public void deleteMember(@AuthenticationPrincipal MemberDetail memberDetail) {
        memberService.removeMember(memberDetail.getMember());
    }
}
