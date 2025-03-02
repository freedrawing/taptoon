package com.sparta.taptoon.global.common.enums;

/*
 * COMPLETED보다는 REGISTERED가 더 어울리는 듯. 바꿔도 돼요, 창현님?
 * 그리고 이름도 ImageStatus에서 Status로 바꾸면 MatchingPost나, Portfolio Entity에서도 사용가능할 거 같습니다...!
 *  처음에 빈 객체 만들 때 상태값이 하나 필요할 거 같아서요!!
 */
public enum Status {
    PENDING,
    REGISTERED,
    DELETING,
    DELETED
    ;

    // 이미지가 실제로 등록이 되었는지 확인
    public static boolean isRegistered(Status status) {
        return status == REGISTERED;
    }
    public static boolean isDeleted(Status status) { return status == DELETED; }
}
