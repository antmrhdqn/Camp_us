package com.commit.campus.service.impl;

import com.commit.campus.common.openfeign.CampingApiClient;
import com.commit.campus.dto.GoCampingDTO;
import com.commit.campus.entity.Camping;
import com.commit.campus.entity.CampingFacilities;
import com.commit.campus.repository.CampingFacilitiesRepository;
import com.commit.campus.repository.CampingRepository;
import com.commit.campus.service.ApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ApiServiceImpl implements ApiService {

    private final CampingApiClient campingApiClient;
    private final ObjectMapper objectMapper;
    private final CampingRepository campingRepository;
    private final CampingFacilitiesRepository campingFacilitiesRepository;

    public ApiServiceImpl(
            CampingApiClient campingApiClient,
            ObjectMapper objectMapper, CampingRepository campingRepository,
            CampingFacilitiesRepository campingFacilitiesRepository) {
        this.campingApiClient = campingApiClient;
        this.objectMapper = objectMapper;
        this.campingRepository = campingRepository;
        this.campingFacilitiesRepository = campingFacilitiesRepository;
    }

    @Value("${gocamping.api.encoding-key}")
    private String serviceKey;

    private static final int NUM_OF_ROWS = 5000;
    private static final int PAGE_NO = 0;
    private static final String VALIDATION_CHECK_OS_KIND = "ETC";
    private static final String VALIDATION_CHECK_APP_NAME = "campus";
    private static final String RESPONSE_FIFE_FORMAT = "json";

    @Override
    public String callCampingApi() {
        String responseJson =
                campingApiClient.getBaseList(
                        NUM_OF_ROWS, PAGE_NO,
                        VALIDATION_CHECK_OS_KIND,
                        VALIDATION_CHECK_APP_NAME,
                        serviceKey, RESPONSE_FIFE_FORMAT);

        return responseJson;
    }

    @Override
    public void saveCampingData() {
        // 1. 저장 시작 전에 기존 데이터 싹 비우기 (딱 한 번만 실행)
        campingFacilitiesRepository.deleteAll();
        campingRepository.deleteAll();
        log.info("=== 기존 데이터 삭제 완료 ===");

        int pageNo = 1; // 1페이지부터 시작
        int numOfRows = 500; // 한 번에 500개씩만 요청 (안전하게)

        while (true) {
            try {
                log.info("=== 페이지 {} 요청 중... ===", pageNo);

                // API 호출 (페이지 번호를 바꿔가며 호출해야 함)
                String campingData = campingApiClient.getBaseList(
                        numOfRows,
                        pageNo,
                        VALIDATION_CHECK_OS_KIND,
                        VALIDATION_CHECK_APP_NAME,
                        serviceKey,
                        RESPONSE_FIFE_FORMAT
                );

                JsonNode rootNode = objectMapper.readTree(campingData);
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                // 데이터가 없으면 반복 종료 (다 가져왔다는 뜻)
                if (itemsNode.isMissingNode() || itemsNode.isNull() || itemsNode.isEmpty()) {
                    log.info("=== 더 이상 데이터가 없습니다. 총 {} 페이지 저장 완료 ===", pageNo - 1);
                    break;
                }

                List<GoCampingDTO> campingDTOList = objectMapper.convertValue(itemsNode, new TypeReference<List<GoCampingDTO>>() {});

                // 저장 로직
                for (GoCampingDTO campingDTO : campingDTOList) {
                    Camping campingEntity = mapToEntity(campingDTO);
                    campingRepository.save(campingEntity);

                    List<CampingFacilities> facilities = checkCampFacsType(campingEntity, campingDTO);
                    campingFacilitiesRepository.saveAll(facilities);
                }

                log.info("=== 페이지 {} 저장 성공 ({} 개) ===", pageNo, campingDTOList.size());

                // 가져온 개수가 요청한 개수보다 적으면 마지막 페이지이므로 종료
                if (campingDTOList.size() < numOfRows) {
                    log.info("=== 모든 데이터 저장 완료 ===");
                    break;
                }

                pageNo++; // 다음 페이지로

            } catch (Exception e) {
                log.error("페이지 {} 저장 중 에러 발생: {}", pageNo, e.getMessage());
                // 에러가 나도 다음 페이지는 시도해보려면 continue, 멈추려면 break
                break;
            }
        }
    }

    private Camping mapToEntity(GoCampingDTO campingDTO) {

        Camping campingEntity = new Camping();

        campingEntity.setContentId(campingDTO.getContentId());
        campingEntity.setCampName(campingDTO.getCampName());
        campingEntity.setLineIntro(campingDTO.getLineIntro());
        campingEntity.setIntro(campingDTO.getIntro());
        campingEntity.setDoName(campingDTO.getDoName());
        campingEntity.setSigunguName(campingDTO.getSigunguName());
        campingEntity.setPostCode(campingDTO.getPostCode());
        campingEntity.setFeatureSummary(campingDTO.getFeatureSummary());
        campingEntity.setInduty(campingDTO.getInduty());
        campingEntity.setAddr(campingDTO.getAddr());
        campingEntity.setAddr(campingDTO.getAddrDetails());
        campingEntity.setMapX(campingDTO.getMapX());
        campingEntity.setMapY(campingDTO.getMapY());
        campingEntity.setTel(campingDTO.getTel());
        campingEntity.setHomepage(campingDTO.getHomepage());
        campingEntity.setStaffCount(campingDTO.getStaffCount());
        campingEntity.setCreatedDate(parseSafeDateTime(campingDTO.getCreatedDate()));
        campingEntity.setLastModifiedDate(parseSafeDateTime(campingDTO.getModifiedDate()));
        campingEntity.setGeneralSiteCnt(campingDTO.getGeneral_site_cnt());
        campingEntity.setCarSiteCnt(campingDTO.getCar_site_cnt());
        campingEntity.setGlampingSiteCnt(campingDTO.getGlamping_site_cnt());
        campingEntity.setCaravanSiteCnt(campingDTO.getCaravan_site_cnt());
        campingEntity.setPersonalCaravanSiteCnt(campingDTO.getPersonal_caravan_site_cnt());
        campingEntity.setSupportFacilities(campingDTO.getSupportFacilities());
        campingEntity.setOutdoorActivities(campingDTO.getOutdoorActivities());
        campingEntity.setPetAccess(campingDTO.getPetAccess());
        campingEntity.setRentalGearList(campingDTO.getRentalGearList());
        campingEntity.setOperationDay(campingDTO.getOperationDay());
        campingEntity.setFirstImageUrl(campingDTO.getFirstImageUrl());

        return campingEntity;

    }

    private List<CampingFacilities> checkCampFacsType(Camping campingEntity, GoCampingDTO campingDTO) {

        List<CampingFacilities> facilities = new ArrayList<>();

        if (campingDTO.getGeneral_site_cnt() > 0) {
            facilities.add(createFacility(campingEntity, campingDTO, 1)); // 1, 일반야영장
        }
        if (campingDTO.getCar_site_cnt() > 0) {
            facilities.add(createFacility(campingEntity, campingDTO, 2)); // 2, 자동차 야영장
        }
        if (campingDTO.getGlamping_site_cnt() > 0) {
            facilities.add(createFacility(campingEntity, campingDTO, 3)); // 3, 글램핑
        }
        if (campingDTO.getCaravan_site_cnt() > 0) {
            facilities.add(createFacility(campingEntity, campingDTO, 4)); // 4, 카라반
        }
        if (campingDTO.getPersonal_caravan_site_cnt() > 0) {
            facilities.add(createFacility(campingEntity, campingDTO, 5)); // 5, 개인카라반
        }

        return facilities;
    }

    private CampingFacilities createFacility(Camping camping, GoCampingDTO campingDTO, int facsTypeId) {

        CampingFacilities facilitiesEntity = new CampingFacilities();

        facilitiesEntity.setCampId(camping.getCampId());
        facilitiesEntity.setFacsTypeId(facsTypeId);
        facilitiesEntity.setInternalFacilitiesList(campingDTO.getInternalFacilitiesList());
        facilitiesEntity.setToiletCnt(campingDTO.getToiletCnt());
        facilitiesEntity.setShowerRoomCnt(campingDTO.getShowerRoomCnt());
        facilitiesEntity.setSinkCnt(campingDTO.getSinkCnt());
        facilitiesEntity.setBrazierClass(campingDTO.getBrazierClass());
        facilitiesEntity.setPersonalTrailerStatus(campingDTO.getPersonalTrailerStatus());
        facilitiesEntity.setPersonalCaravanStatus(campingDTO.getPersonalCaravanStatus());

        return facilitiesEntity;
    }

    // 날짜 변환기: "2023-01-01 13:00:00"도 처리하고 "2023-01-01"도 처리함
    private LocalDateTime parseSafeDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            // 1. "yyyy-MM-dd HH:mm:ss" 포맷 시도
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            try {
                // 2. 실패하면 "yyyy-MM-dd" (날짜만 있는 경우) 시도
                return LocalDate.parse(dateStr).atStartOfDay(); // 00:00:00 시간을 자동으로 붙여줌
            } catch (DateTimeParseException ex) {
                // 3. 이것도 아니면 그냥 null (데이터가 꼬인 경우)
                return null;
            }
        }
    }
}
