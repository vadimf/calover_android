package com.varteq.catslovers.model;

import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.List;

public class QBPostComment extends QBCustomObject {

    public static final String CLASS_NAME = "PostComment";

    public static final String MESSAGE_FIELD = "message";
    public static final String POST_ID_FIELD = "post_id";

    public QBPostComment(String message, String postId) {
        putString(MESSAGE_FIELD, message);
        putString(POST_ID_FIELD, postId);
        setClassName(CLASS_NAME);
    }

    public QBPostComment(String id, String message, String postId) {
        this(message, postId);
        setCustomObjectId(id);
    }

    public static PostComment toPostComment(QBCustomObject object, String avatar, String userName) {

        return new PostComment(object.getCustomObjectId(), Integer.parseInt(object.getString(POST_ID_FIELD)),
                object.getUserId(), avatar, userName,
                object.getCreatedAt(), object.getString(MESSAGE_FIELD));
    }

    public static QBRequestGetBuilder getRequestBuilder(List<String> ids) {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.addRule(POST_ID_FIELD, "[in]", QBFeedPost.arrayToString(ids));
        requestBuilder.sortAsc("created_at");
        return requestBuilder;
    }
}
