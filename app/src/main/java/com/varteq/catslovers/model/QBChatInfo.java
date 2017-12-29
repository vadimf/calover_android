package com.varteq.catslovers.model;

import com.quickblox.chat.model.QBDialogCustomData;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QueryRule;

import java.util.ArrayList;
import java.util.List;

public class QBChatInfo extends QBDialogCustomData {

    public static final String CLASS_NAME = "ChatInfo";

    public static final String FEEDSTATION_ID_FIELD = "feedstation_id";
    public static final String INVITED_USERS_FIELD = "invited_users";

    public QBChatInfo(Integer stationId) {
        super(CLASS_NAME);
        putInteger(FEEDSTATION_ID_FIELD, stationId);
    }

    public static QBRequestGetBuilder getRequestBuilder(Integer stationId) {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.addRule("data[class_name]", QueryRule.EQ, CLASS_NAME);
        requestBuilder.addRule("data[" + FEEDSTATION_ID_FIELD + "]", QueryRule.EQ, stationId);
        return requestBuilder;
    }

    public static QBDialogRequestBuilder getDialogRequestForAddInvitedUser(String userId) {
        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
        qbRequestBuilder.pushAll(INVITED_USERS_FIELD, userId);
        return qbRequestBuilder;
    }

    public static QBDialogRequestBuilder getDialogRequestForRemoveInvitedUser(Integer userId) {
        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
        qbRequestBuilder.pullAll(INVITED_USERS_FIELD, userId);
        return qbRequestBuilder;
    }

    public static boolean containsInvitedUser(QBDialogCustomData data, String userId) {
        List<String> list = getInvitedUsers(data);
        if (data != null && userId != null) {
            for (String item : list) {
                if (item.equals(userId))
                    return true;
            }
            return false;
        } else return false;
    }

    public static List<String> getInvitedUsers(QBDialogCustomData data) {
        if (data != null) {
            List<Object> list = data.getArray(INVITED_USERS_FIELD);
            if (list == null || list.isEmpty()) return null;
            List<String> result = new ArrayList<>();
            for (Object o : list)
                result.add((String) o);
            return result;
        } else return null;
    }
}
