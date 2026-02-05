package com.gntcyouthbe.church.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ChurchId {
    ANYANG("안양"),
    SUWON("수원"),
    ANSAN("안산"),
    GWACHEON("과천"),
    SIHEUNG("시흥"),
    GWANGMYEONG("광명"),
    BUPYEONG("부평"),
    BUGOK("부곡"),
    PANGYO("판교"),
    YEONGDEUNGPO("영등포"),
    INCHEON("인천"),
    BUCHEON("부천"),
    ILSAN("일산"),
    SIHWA("시화"),
    YEONGTONG("영통"),
    GURI("구리"),
    POIL("포일"),
    JEONWON("전원"),
    GIMPO("김포"),
    PYEONGTAEK("평택"),
    ANJUNG("안중"),
    CHEONAN("천안"),
    YANGJU("양주"),
    GANGNAM("강남"),
    YONGIN("용인"),
    DAEJEON("대전"),
    GWANGJU("광주"),
    SEOSAN("서산"),
    YULJEON("율전"),
    DONGTAN("동탄"),
    DANGJIN("당진"),
    SEJONG("세종"),
    JEONJU_HYOJA("전주효자");

    private final String displayName;
}
