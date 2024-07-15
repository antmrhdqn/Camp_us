package com.commit.camp_reserve.service;

import com.commit.camp_reserve.dto.CampingApiResponse;
import com.commit.camp_reserve.dto.CampingDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
public class CampingService {

    private final RestTemplate restTemplate;

    @Value("${gocamping.api.basedList}")
    private String basedList;

    @Value("${gocamping.api.key}")
    private String serviceKey;

    public CampingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<CampingDto> getBasedList() {
        String uriStr = String.format("%s?MobileOS=WIN&MobileApp=Camp_Reserve&serviceKey=%s&_type=json",
                basedList, serviceKey);

        URI uri = URI.create(uriStr);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            String responseBody = response.getBody();
            System.out.println("API Response: " + responseBody);
            // Convert JSON response to CampingDto list
            return parseCampingDtoList(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<CampingDto> parseCampingDtoList(String responseBody) {
        Gson gson = new Gson();
        Type responseType = new TypeToken<CampingApiResponse>() {}.getType();
        CampingApiResponse apiResponse = gson.fromJson(responseBody, responseType);
        if (apiResponse != null && apiResponse.getResponse() != null && apiResponse.getResponse().getBody() != null) {
            return apiResponse.getResponse().getBody().getItems().getItem();
        }
        return Collections.emptyList();
    }
}

