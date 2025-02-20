package com.sparta.taptoon.domain.util;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

import static com.sparta.taptoon.domain.util.FakerUtil.faker;

@UtilityClass
public class EntityCreatorUtil {

    public static Member createMember() {
        return Member.builder()
                .email(faker.internet().emailAddress())
                .name(faker.name().firstName())
                .nickname(faker.name().nameWithMiddle())
                .password(faker.internet().password())
                .build();
    }

    public static MatchingPost createMatchingPost(Member member) {
        return MatchingPost.builder()
                .writer(member)
                .artistType(ArtistType.random())
                .title(faker.lorem().word())
                .workType(WorkType.random())
                .description(faker.lorem().sentence())
                .build();
    }

    public static MatchingPostImage createMatchingPostImage(MatchingPost matchingPost) {
        return MatchingPostImage.builder()
                .matchingPost(matchingPost)
                .imageUrl(MessageFormat.format("{0}/{1}", faker.internet().url(), faker.file().fileName()))
                .build();
    }

}
