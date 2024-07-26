package com.commit.campus.repository;

import com.commit.campus.entity.RatingSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
@Slf4j
public class RatingSummaryRepository {

    private final DynamoDbTable<RatingSummary> ratingSummaryTable;

    public RatingSummaryRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.ratingSummaryTable = dynamoDbEnhancedClient.table("RATING_SUMMARY", TableSchema.fromBean(RatingSummary.class));
    }

    public void ratingUpdate(Long campId, Byte rating) {
        Key key = Key.builder().partitionValue(campId).build();
        log.info("서머리 확인 key {}", key);
        AttributeValue partitionValue = key.partitionKeyValue();
        log.info("Partition key value: {}", partitionValue.n());
        RatingSummary ratingSummary = ratingSummaryTable.getItem(key);
        log.info("check RatingSummary {}", ratingSummary);
        if (ratingSummary != null) {
            log.info("Found item: {}", ratingSummary);
        } else {
            log.info("No item found for key: {}", key);
        }

        if (ratingSummary != null) {

            log.info("Found existing item: {}", ratingSummary);
            ratingSummary.setTotalRating(ratingSummary.getTotalRating() + rating);
            ratingSummary.setCountRating(ratingSummary.getCountRating() + 1);
        } else {

            log.info("No existing item found for key: {}", key);
            ratingSummary = new RatingSummary();
            ratingSummary.setCampId(campId);
            ratingSummary.setTotalRating(rating);
            ratingSummary.setCountRating(1);
        }

        ratingSummaryTable.putItem(ratingSummary);
    }
}
