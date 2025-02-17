package com.sparta.taptoon.domain.util;

import com.github.javafaker.Faker;
import lombok.experimental.UtilityClass;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class FakerUtil {

    public static final Faker faker = new Faker(new Locale("en"));
    public static final Faker koreanFaker = new Faker(new Locale("ko"));

    void FAKER_템플릿() {
        // 기본 개인정보
        String firstName = faker.name().firstName();          // 이름
        String lastName = faker.name().lastName();            // 성
        String fullName = faker.name().fullName();
        String email = String.format("%s_%s@test.com", firstName, lastName);  // 이메일 생성
        int age = faker.number().numberBetween(20, 70);      // 20-70 사이 나이

        // 텍스트 생성
        String dummyText = faker.letterify("?????");         // ?를 랜덤 문자로 치환
        String word = faker.lorem().word();                  // 랜덤 단어
        String description = faker.lorem().paragraph(3_000);  // 3000자 문단

        // 날짜 관련
        Date randomDate = faker.date().birthday();           // 랜덤 생일
        Date birthday = faker.date().birthday();             // 생일
        Date pastDate = faker.date().past(1000, TimeUnit.DAYS);    // 과거 1000일 이내
        Date futureDate = faker.date().future(500, TimeUnit.DAYS); // 미래 500일 이내

        // 연락처
        String phoneNumber = faker.phoneNumber().phoneNumber(); // 일반 전화번호
        String cellPhone = faker.phoneNumber().cellPhone();    // 휴대폰 번호

        // 주소 관련
        String street = faker.address().streetAddress();     // 도로명 주소
        String city = faker.address().city();               // 도시
        String country = faker.address().country();         // 국가
        String zipCode = faker.address().zipCode();         // 우편번호

        // 금융 관련
        String creditCard = faker.finance().creditCard();    // 신용카드 번호

        // 회사 관련
        String companyName = faker.company().name();        // 회사명
        String industry = faker.company().industry();       // 산업 분야

        // 직업 관련
        String jobTitle = faker.job().title();             // 직책
        String position = faker.job().position();          // 직위
        String field = faker.job().field();               // 업무 분야
        String seniority = faker.job().seniority();       // 연차/서열

        // 인터넷 관련
        String email2 = faker.internet().emailAddress();    // 이메일 주소
        String password = faker.internet().password();      // 비밀번호
        String domain = faker.internet().domainName();      // 도메인 이름

        // 책 관련
        String bookTitle = faker.book().title();           // 책 제목
        String author = faker.book().author();             // 저자
        String publisher = faker.book().publisher();       // 출판사
        String genre = faker.book().genre();              // 장르

        // 파일 관련
        String fileName = faker.file().fileName();           // 파일명
        String extension = faker.file().extension();         // 파일 확장자
        String mimeType = faker.file().mimeType();          // MIME 타입

        // 색상 관련
        String colorName = faker.color().name();            // 색상 이름
        String hexColor = faker.color().hex();              // HEX 컬러코드

        // 숫자 관련
        int randomNumber = faker.number().randomDigit();    // 0-9 사이 랜덤 숫자
        double randomDouble = faker.number().randomDouble(2, 0, 100);  // 소수점 2자리, 0-100 사이
        int numberBetween = faker.number().numberBetween(1, 10);      // 1-10 사이 정수

        // 시간 관련
        String timeZone = faker.address().timeZone();       // 타임존
        Date between = faker.date().between(new Date(), new Date());      // 특정 기간 내 랜덤 날짜

        // ID 관련
        String uuid = faker.internet().uuid();              // UUID
        String macAddress = faker.internet().macAddress();  // MAC 주소
        String ipV4 = faker.internet().ipV4Address();       // IPv4 주소
        String ipV6 = faker.internet().ipV6Address();       // IPv6 주소

        // 상품 관련
        String product = faker.commerce().productName();     // 상품명
        String price = faker.commerce().price();            // 가격
        String department = faker.commerce().department();   // 부서/카테고리

        // 코드 관련
        String isbn10 = faker.code().isbn10();             // ISBN-10
        String isbn13 = faker.code().isbn13();             // ISBN-13
        String imei = faker.code().imei();                 // IMEI
    }

}
