package com.sparta.taptoon.global.util;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;

@Deprecated
public class MySQLFullTextFunction implements FunctionContributor {
    // 1) Boolean Mode용
    private static final String FUNCTION_NAME_BOOLEAN = "match_against_boolean";
    private static final String FUNCTION_PATTERN_BOOLEAN = "MATCH(?1, ?2) AGAINST(?3 IN BOOLEAN MODE)";

    // 2) Natural Language Mode용
    private static final String FUNCTION_NAME_NL = "match_against_nl";
    private static final String FUNCTION_PATTERN_NL = "MATCH(?1, ?2) AGAINST(?3 IN NATURAL LANGUAGE MODE)";

    @Override
    public void contributeFunctions(FunctionContributions configuration) {
        // DOUBLE 타입으로 반환 설정
        BasicType<Double> doubleType = configuration.getTypeConfiguration()
                .getBasicTypeRegistry()
                .resolve(StandardBasicTypes.DOUBLE);

        // 1) Boolean 모드 함수 등록
        configuration.getFunctionRegistry()
                .registerPattern(FUNCTION_NAME_BOOLEAN, FUNCTION_PATTERN_BOOLEAN, doubleType);

        // 2) Natural Language 모드 함수 등록
        configuration.getFunctionRegistry()
                .registerPattern(FUNCTION_NAME_NL, FUNCTION_PATTERN_NL, doubleType);
    }
}
