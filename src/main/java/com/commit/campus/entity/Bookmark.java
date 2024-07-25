package com.commit.campus.entity;

import com.commit.campus.dto.CampingForDynamoDB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Bookmark {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String userId;
    private String campId;
    private String campInfo;
    private String createdBookmarkDate;
    private CampingForDynamoDB campingForDynamoDB;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("user_id")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDbAttribute("camp_id")
    public String getCampId() {
        return campId;
    }

    public void setCampId(String campId) {
        this.campId = campId;
    }

    @DynamoDbAttribute("camp_info")
    public String getCampInfo() {
        return campInfo;
    }

    public void setCampInfo(String campInfo) {
        this.campInfo = campInfo;
        if (campInfo != null) {
            try {
                this.campingForDynamoDB = OBJECT_MAPPER.readValue(campInfo, CampingForDynamoDB.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    @DynamoDbAttribute("created_bookmark_date")
    public String getCreatedBookmarkDate() {
        return createdBookmarkDate;
    }

    public void setCreatedBookmarkDate(String createdBookmarkDate) {
        this.createdBookmarkDate = createdBookmarkDate;
    }

    @DynamoDbIgnore
    public CampingForDynamoDB getCampingInfo() {
        if (campingForDynamoDB == null && campInfo != null) {
            try {
                this.campingForDynamoDB = OBJECT_MAPPER.readValue(campInfo, CampingForDynamoDB.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return campingForDynamoDB;
    }

    public void setCampingInfo(CampingForDynamoDB campingForDynamoDB) {
        this.campingForDynamoDB = campingForDynamoDB;
        try {
            this.campInfo = OBJECT_MAPPER.writeValueAsString(campingForDynamoDB);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
