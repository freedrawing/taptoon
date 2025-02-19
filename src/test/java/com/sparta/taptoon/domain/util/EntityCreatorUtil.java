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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.sparta.taptoon.domain.util.FakerUtil.englishFaker;
import static com.sparta.taptoon.domain.util.FakerUtil.koreanFaker;

@UtilityClass
public class EntityCreatorUtil {

    private String generateRandomSentence(Faker faker) {
        List<String> words = faker.lorem().words(20);  // 20개의 단어를 랜덤으로 가져옴
        return words.stream().collect(Collectors.joining(" "));  // 단어들을 공백으로 조합
    }

    public static Member createMember() {
        return Member.builder()
                .email(englishFaker.internet().emailAddress())
                .name(koreanFaker.name().firstName())
                .nickname(koreanFaker.name().nameWithMiddle())
                .password(englishFaker.internet().password())
                .grade(MemberGrade.BASIC)
                .isDeleted(false)
                .build();
    }

    public static AddMatchingPostRequest createKoreanMatchingPostRequest() {
        return new AddMatchingPostRequest(
                generateRandomSentence(koreanFaker),
                ArtistType.random().name(),
                WorkType.random().name(),
                generateRandomSentence(koreanFaker)
        );
    }

    public static AddMatchingPostRequest createEnglishMatchingPostRequest() {
        return new AddMatchingPostRequest(
                generateRandomSentence(englishFaker),
                ArtistType.random().name(),
                WorkType.random().name(),
                generateRandomSentence(englishFaker)
        );
    }

    public static MatchingPost createMatchingPost(Member member) {
        return MatchingPost.builder()
                .writer(member)
                .artistType(ArtistType.random())
                .title(koreanFaker.lorem().word())
                .workType(WorkType.random())
                .description(koreanFaker.lorem().sentence())
                .build();
    }

    public static MatchingPostImage createMatchingPostImage(MatchingPost matchingPost) {
        return MatchingPostImage.builder()
                .matchingPost(matchingPost)
                .imageUrl(MessageFormat.format("{0}/{1}", englishFaker.internet().url(), englishFaker.file().fileName()))
                .build();
    }

}
