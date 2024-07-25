package com.commit.campus.repository;


import com.commit.campus.entity.Bookmark;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;


@Repository
public class BookmarkRepository {

    private final DynamoDbTable<Bookmark> bookmarkDynamoDBTable;

    public BookmarkRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.bookmarkDynamoDBTable = dynamoDbEnhancedClient.table("Bookmark", TableSchema.fromBean(Bookmark.class));
    }

    public void save(Bookmark bookmark) {
        bookmarkDynamoDBTable.putItem(bookmark);
    }


}
