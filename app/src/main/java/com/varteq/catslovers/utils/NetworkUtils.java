package com.varteq.catslovers.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.quickblox.auth.session.QBSessionManager;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.model.QBFeedPost;

import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class NetworkUtils {

    public static ErrorData parseError(Response<?> response) {
        Converter<ResponseBody, ErrorData> converter = ServiceGenerator.retrofit()
                .responseBodyConverter(ErrorData.class, new Annotation[0]);

        ErrorData error;

        try {
            error = converter.convert(response.errorBody());
        } catch (Exception e) {
            return null;//new ErrorData();
        }

        return error;
    }

    public static boolean isNetworkErr(String e) {
        return e.contains("UnknownHost") || e.contains("Network is unreachable") || e.contains("HttpHostConnectException")
                || e.contains("Timeout") || e.contains("Unable to execute HTTP request") || e.contains("Unable to resolve host") || e.contains("ConnectException");
    }

    public static boolean isNetworkErr(Exception e) {
        return isNetworkErr(e.toString());
    }

    /**
     * -H "QuickBlox-REST-API-Version: 0.1.0" \
     * -H "QB-Token: 69217f8cfa59ebbb42d610ccb8ac1e55988cadd7" \
     * https://api.quickblox.com/blobs/43234/download.json
     */
    public static GlideUrl getUserAvatarGlideUrl(String avatarId) {
        if (!ChatHelper.getInstance().isLogged())
            return null;
        GlideUrl glideUrl = new GlideUrl("https://api.quickblox.com/blobs/" + avatarId + "/download.json",
                new LazyHeaders.Builder()
                        .addHeader("QuickBlox-REST-API-Version", "0.1.0")
                        .addHeader("QB-Token", QBSessionManager.getInstance().getToken())
                        .build());
        return glideUrl;
    }

    /*"QB-Token: 53247fce22672e627880bf9a7060aadecc2c3e59"
            -d"field_name=avatar" https://api.quickblox.com/data/<Class_name>/<record_id>/file.json*/
    public static GlideUrl getFeedPostPreviewGlideUrl(String feedPostId) {
        if (!ChatHelper.getInstance().isLogged())
            return null;
        GlideUrl glideUrl = new GlideUrl("https://api.quickblox.com/data/" + QBFeedPost.CLASS_NAME + "/" + feedPostId +
                "/file.json?field_name=" + QBFeedPost.PREVIEW_FIELD,
                new LazyHeaders.Builder()
                        .addHeader("QB-Token", QBSessionManager.getInstance().getToken())
                        .build());
        return glideUrl;
    }
}

