package com.sparta.taptoon.domain.portfolio.dto.request;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePortfolioRequest {

    private User user;
    private MatchingPost matchingPost;
    private String content;

}
