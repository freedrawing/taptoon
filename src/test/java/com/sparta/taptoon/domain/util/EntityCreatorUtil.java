package com.sparta.taptoon.domain.util;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import lombok.experimental.UtilityClass;

import static com.sparta.taptoon.domain.util.FakerUtil.faker;

@UtilityClass
public class EntityCreatorUtil {

    public static Member createMember() {
        return Member.builder()
                .email(faker.internet().emailAddress())
                .name(faker.name().firstName())
                .nickname(faker.name().nameWithMiddle())
                .password(faker.internet().password())
                .grade(MemberGrade.BASIC)
                .isDeleted(false)
                .build();
    }

    public static MatchingPost createMatchingPost(Member member) {
        return MatchingPost.builder()
                .writer(member)
                .artistType(ArtistType.ILLUSTRATOR)
                .title(faker.lorem().word())
                .workType(WorkType.HYBRID)
                .description(faker.lorem().sentence())
                .build();
    }
}
