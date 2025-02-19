package com.sparta.taptoon.domain.util;

import com.github.javafaker.Faker;
import lombok.experimental.UtilityClass;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class FakerUtil {

    public static final Faker englishFaker = new Faker(new Locale("en"));
    public static final Faker koreanFaker = new Faker(new Locale("ko"));

    void FAKER_템플릿() {
        // 기본 개인정보
        String firstName = englishFaker.name().firstName();          // 이름
        String lastName = englishFaker.name().lastName();            // 성
        String fullName = englishFaker.name().fullName();
        String email = String.format("%s_%s@test.com", firstName, lastName);  // 이메일 생성
        int age = englishFaker.number().numberBetween(20, 70);      // 20-70 사이 나이

        // 텍스트 생성
        String dummyText = englishFaker.letterify("?????");         // ?를 랜덤 문자로 치환
        String word = englishFaker.lorem().word();                  // 랜덤 단어
        String description = englishFaker.lorem().paragraph(3_000);  // 3000자 문단

        // 날짜 관련
        Date randomDate = englishFaker.date().birthday();           // 랜덤 생일
        Date birthday = englishFaker.date().birthday();             // 생일
        Date pastDate = englishFaker.date().past(1000, TimeUnit.DAYS);    // 과거 1000일 이내
        Date futureDate = englishFaker.date().future(500, TimeUnit.DAYS); // 미래 500일 이내

        // 연락처
        String phoneNumber = englishFaker.phoneNumber().phoneNumber(); // 일반 전화번호
        String cellPhone = englishFaker.phoneNumber().cellPhone();    // 휴대폰 번호

        // 주소 관련
        String street = englishFaker.address().streetAddress();     // 도로명 주소
        String city = englishFaker.address().city();               // 도시
        String country = englishFaker.address().country();         // 국가
        String zipCode = englishFaker.address().zipCode();         // 우편번호

        // 금융 관련
        String creditCard = englishFaker.finance().creditCard();    // 신용카드 번호

        // 회사 관련
        String companyName = englishFaker.company().name();        // 회사명
        String industry = englishFaker.company().industry();       // 산업 분야

        // 직업 관련
        String jobTitle = englishFaker.job().title();             // 직책
        String position = englishFaker.job().position();          // 직위
        String field = englishFaker.job().field();               // 업무 분야
        String seniority = englishFaker.job().seniority();       // 연차/서열

        // 인터넷 관련
        String email2 = englishFaker.internet().emailAddress();    // 이메일 주소
        String password = englishFaker.internet().password();      // 비밀번호
        String domain = englishFaker.internet().domainName();      // 도메인 이름

        // 책 관련
        String bookTitle = englishFaker.book().title();           // 책 제목
        String author = englishFaker.book().author();             // 저자
        String publisher = englishFaker.book().publisher();       // 출판사
        String genre = englishFaker.book().genre();              // 장르

        // 파일 관련
        String fileName = englishFaker.file().fileName();           // 파일명
        String extension = englishFaker.file().extension();         // 파일 확장자
        String mimeType = englishFaker.file().mimeType();          // MIME 타입

        // 색상 관련
        String colorName = englishFaker.color().name();            // 색상 이름
        String hexColor = englishFaker.color().hex();              // HEX 컬러코드

        // 숫자 관련
        int randomNumber = englishFaker.number().randomDigit();    // 0-9 사이 랜덤 숫자
        double randomDouble = englishFaker.number().randomDouble(2, 0, 100);  // 소수점 2자리, 0-100 사이
        int numberBetween = englishFaker.number().numberBetween(1, 10);      // 1-10 사이 정수

        // 시간 관련
        String timeZone = englishFaker.address().timeZone();       // 타임존
        Date between = englishFaker.date().between(new Date(), new Date());      // 특정 기간 내 랜덤 날짜

        // ID 관련
        String uuid = englishFaker.internet().uuid();              // UUID
        String macAddress = englishFaker.internet().macAddress();  // MAC 주소
        String ipV4 = englishFaker.internet().ipV4Address();       // IPv4 주소
        String ipV6 = englishFaker.internet().ipV6Address();       // IPv6 주소

        // 상품 관련
        String product = englishFaker.commerce().productName();     // 상품명
        String price = englishFaker.commerce().price();            // 가격
        String department = englishFaker.commerce().department();   // 부서/카테고리

        // 코드 관련
        String isbn10 = englishFaker.code().isbn10();             // ISBN-10
        String isbn13 = englishFaker.code().isbn13();             // ISBN-13
        String imei = englishFaker.code().imei();                 // IMEI
    }

}
