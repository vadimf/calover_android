package com.varteq.catslovers.utils;

import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.ErrorData;

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
                || e.contains("Timeout") || e.contains("Unable to execute HTTP request");
    }

    public static boolean isNetworkErr(Exception e) {
        return isNetworkErr(e.toString());
    }
}

