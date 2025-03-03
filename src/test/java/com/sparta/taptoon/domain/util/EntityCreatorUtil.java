package com.sparta.taptoon.domain.util;

import com.github.javafaker.Faker;
import com.sparta.taptoon.domain.matchingpost.dto.request.AddMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.MemberGrade;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;
import java.util.Locale;

import static com.sparta.taptoon.domain.util.FakerUtil.englishFaker;
import static com.sparta.taptoon.domain.util.FakerUtil.koreanFaker;

@UtilityClass
public class EntityCreatorUtil {

    public static Member createMember() {
        return Member.builder()
                .email(englishFaker.internet().emailAddress())
                .name(koreanFaker.name().firstName())
                .nickname(koreanFaker.name().nameWithMiddle())
                .password(englishFaker.internet().password())
                .build();
    }

    public static AddMatchingPostRequest createKoreanMatchingPostRequest() {
        return new AddMatchingPostRequest(
//                generateRandomSentence(koreanFaker),
                new Faker(new Locale("ko")).commerce().department(),
                ArtistType.random().name(),
                WorkType.random().name(),
//                generateRandomSentence(koreanFaker)
                new Faker(new Locale("ko")).lorem().paragraph(2)
        );
    }

    public static AddMatchingPostRequest createEnglishMatchingPostRequest() {
        return new AddMatchingPostRequest(
                new Faker(new Locale("en")).commerce().department(),
//                generateRandomSentence(englishFaker),
                ArtistType.random().name(),
                WorkType.random().name(),
//                generateRandomSentence(englishFaker)
                new Faker(new Locale("en")).lorem().paragraph(2)
        );
    }

    public static MatchingPost createMatchingPost(Member member) {
        return MatchingPost.builder()
                .author(member)
                .artistType(ArtistType.random())
                .title(koreanFaker.lorem().word())
                .workType(WorkType.random())
                .description(koreanFaker.lorem().sentence())
                .build();
    }

//    public static MatchingPostImage createMatchingPostImage(MatchingPost matchingPost) {
//        return MatchingPostImage.builder()
//                .matchingPost(matchingPost)
//                .thumbnailImageUrl(MessageFormat.format("{0}/{1}", englishFaker.internet().url(), englishFaker.file().fileName()))
//                .build();
//    }

}
