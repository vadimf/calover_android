package com.varteq.catslovers.model;

import android.net.Uri;

import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.Iterator;
import java.util.List;

public class QBFeedPost extends QBCustomObject {

    public static final String CLASS_NAME = "Feed";

    public static final String PICTURE_FIELD = "picture";
    public static final String VIDEO_FIELD = "video";
    public static final String STATION_ID_FIELD = "station_id";

    public QBFeedPost(String message, String id) {
        putString("description", message);
        putString(STATION_ID_FIELD, id);
        setClassName(CLASS_NAME);
    }

    public static FeedPost toFeedPost(QBCustomObject object, Uri avatarUri, String userName) {
        FeedPost.FeedPostType type = FeedPost.FeedPostType.TEXT;
        Uri mediaUri = null;
        /*if (!object.getString(VIDEO_FIELD).equals("null")) {
            mediaUri = Uri.parse(object.getString(VIDEO_FIELD));
            type = FeedPost.FeedPostType.VIDEO;
        } else if (!object.getString(PICTURE_FIELD).equals("null")) {
            mediaUri = Uri.parse(object.getString(PICTURE_FIELD));
            type = FeedPost.FeedPostType.PICTURE;
        }*/

        object.getString(STATION_ID_FIELD);
        return new FeedPost(object.getCustomObjectId(), object.getCreatedAt(),
                avatarUri,
                userName,
                object.getString("description"), mediaUri, type);
    }

    public static QBRequestGetBuilder getRequestBuilder(List<String> ids) {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        //requestBuilder.in(STATION_ID_FIELD, getFeedstationIds());
        requestBuilder.addRule(STATION_ID_FIELD, "[in]", arrayToString(ids));
        requestBuilder.sortAsc("created_at");
        return requestBuilder;
    }

    protected static String arrayToString(List<String> values) {
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
