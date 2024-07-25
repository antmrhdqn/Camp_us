package com.commit.campus.repository;

import com.commit.campus.entity.RatingSummary;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class RatingSummaryRepository {

    private final DynamoDbTable<RatingSummary> ratingSummaryDynamoDbTable;

    public RatingSummaryRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.ratingSummaryDynamoDbTable = dynamoDbEnhancedClient.table("RATING_SUMMARY", TableSchema.fromBean(RatingSummary.class));
    }

    public void save(RatingSummary ratingSummary) {
        ratingSummaryDynamoDbTable.putItem(ratingSummary);
    }
}
