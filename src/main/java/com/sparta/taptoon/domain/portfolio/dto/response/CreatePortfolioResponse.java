package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreatePortfolioResponse {

    private User user;
    private MatchingPost matchingPost;
    private String content;
    private LocalDateTime createdAt;

}
