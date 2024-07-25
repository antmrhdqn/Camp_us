package com.commit.campus.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class CampingInfo {
    private Long campId;  // 캠핑장 ID
    private String factNm;  // 시설 이름
    private String lineIntro;  // 한 줄 소개
    private String intro;  // 소개
    private String doNm;  // 도 이름
    private String sigunguNm;  // 시군구 이름
    private String zipcode;  // 우편번호
    private String addr1;  // 주소1
    private String addr2;  // 주소2
    private double mapX;  // 지도 X 좌표
    private double mapY;  // 지도 Y 좌표
    private String tel;  // 전화번호
    private String homepage;  // 홈페이지
    private String manageNmpr;  // 관리 인원

    public Long getCampId() {
        return campId;
    }

    public void setCampId(Long campId) {
        this.campId = campId;
    }

    public String getFactNm() {
        return factNm;
    }

    public void setFactNm(String factNm) {
        this.factNm = factNm;
    }

    public String getLineIntro() {
        return lineIntro;
    }

    public void setLineIntro(String lineIntro) {
        this.lineIntro = lineIntro;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getDoNm() {
        return doNm;
    }

    public void setDoNm(String doNm) {
        this.doNm = doNm;
    }

    public String getSigunguNm() {
        return sigunguNm;
    }

    public void setSigunguNm(String sigunguNm) {
        this.sigunguNm = sigunguNm;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    public double getMapX() {
        return mapX;
    }

    public void setMapX(double mapX) {
        this.mapX = mapX;
    }

    public double getMapY() {
        return mapY;
    }

    public void setMapY(double mapY) {
        this.mapY = mapY;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getManageNmpr() {
        return manageNmpr;
    }

    public void setManageNmpr(String manageNmpr) {
        this.manageNmpr = manageNmpr;
    }
}
