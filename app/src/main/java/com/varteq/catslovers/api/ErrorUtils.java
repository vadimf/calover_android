package com.varteq.catslovers.api;

import com.varteq.catslovers.api.entity.ErrorData;

import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class ErrorUtils {

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
}

