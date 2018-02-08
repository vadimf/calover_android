package com.varteq.catslovers.model;

import android.net.Uri;

import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QBFeedPost extends QBCustomObject {

    public static final String CLASS_NAME = "Feed";

    public static final String PICTURE_FIELD = "picture";
    public static final String VIDEO_FIELD = "video";
    public static final String STATION_ID_FIELD = "station_id";
    public static final String PREVIEW_FIELD = "preview";
    public static final String USERS_LIKED_FIELD = "users_liked_post";

    public QBFeedPost(String message, String stationId) {
        putString("description", message);
        putString(STATION_ID_FIELD, stationId);
        setClassName(CLASS_NAME);
    }

    public QBFeedPost(String id, List<Integer> usersLiked, String message, String stationId) {
        this(message, stationId);
        setCustomObjectId(id);
        putArray(USERS_LIKED_FIELD, usersLiked);
    }

    public QBFeedPost(String id) {
        setCustomObjectId(id);
        setClassName(CLASS_NAME);
    }

    public static FeedPost toFeedPost(QBCustomObject object, String avatar, String userName) {
        FeedPost.FeedPostType type = FeedPost.FeedPostType.TEXT;
        Uri mediaUri = null;
        String mediaName = null;
        String previewName = null;
        if (!object.getString(VIDEO_FIELD).equals("null")) {
            //mediaUri = Uri.parse(object.getString(VIDEO_FIELD));
            mediaName = object.getString(VIDEO_FIELD);
            previewName = object.getString(PREVIEW_FIELD);
            type = FeedPost.FeedPostType.VIDEO;
        } else if (!object.getString(PICTURE_FIELD).equals("null")) {
            //mediaUri = Uri.parse(object.getString(PICTURE_FIELD));
            mediaName = object.getString(PICTURE_FIELD);
            type = FeedPost.FeedPostType.PICTURE;
        }

        List<Object> usersObjects = object.getArray(USERS_LIKED_FIELD);
        List<Integer> usersIds = null;
        if (usersObjects != null && !usersObjects.isEmpty()) {
            usersIds = new ArrayList<>();
            for (Object o : usersObjects)
                usersIds.add(Integer.parseInt((String) o));
        }

        return new FeedPost(object.getCustomObjectId(), object.getUserId(), object.getCreatedAt(),
                avatar,
                userName,
                object.getString("description"), previewName, mediaName, usersIds, Integer.parseInt(object.getString(STATION_ID_FIELD)), type);
    }

    public static QBRequestGetBuilder getRequestBuilder(List<String> ids) {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        //requestBuilder.in(STATION_ID_FIELD, getFeedstationIds());
        requestBuilder.addRule(STATION_ID_FIELD, "[in]", arrayToString(ids));
        requestBuilder.sortAsc("created_at");
        return requestBuilder;
    }

    public static String arrayToString(List<String> values) {
        StringBuilder arrayString = new StringBuilder();
        String delimiter = "";

        for (Iterator var4 = values.iterator(); var4.hasNext(); delimiter = ",") {
            Object obj = var4.next();
            arrayString.append(delimiter);
            arrayString.append(obj);
        }

        return arrayString.toString().replace("[", "").replace("]", "");
    }
}
