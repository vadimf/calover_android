package com.varteq.catslovers.model;

import android.net.Uri;

import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.Iterator;
import java.util.List;

public class QBFeedPost extends QBCustomObject {

    public static final String CLASS_NAME = "Feed";

    public QBFeedPost(String message, String id) {
        putString("description", message);
        putString("station_id", id);
        setClassName(CLASS_NAME);
    }

    public static FeedPost toFeedPost(QBCustomObject object, Uri avatarUri, String userName) {
        FeedPost.FeedPostType type = FeedPost.FeedPostType.TEXT;
        Uri mediaUri = null;
        if (!object.getString("video").equals("null")) {
            mediaUri = Uri.parse(object.getString("video"));
            type = FeedPost.FeedPostType.VIDEO;
        } else if (!object.getString("picture").equals("null")) {
            mediaUri = Uri.parse(object.getString("picture"));
            type = FeedPost.FeedPostType.PICTURE;
        }

        object.getString("station_id");
        return new FeedPost(object.getCustomObjectId(), object.getCreatedAt(),
                avatarUri,
                userName,
                object.getString("description"), mediaUri, type);
    }

    public static QBRequestGetBuilder getRequestBuilder(List<String> ids) {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        //requestBuilder.in("station_id", getFeedstationIds());
        requestBuilder.addRule("station_id", "[in]", arrayToString(ids));
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
