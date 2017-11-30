package com.varteq.catslovers.api;

import com.varteq.catslovers.api.entity.ErrorResponse;

import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class ErrorUtils {

    public static ErrorResponse parseError(Response<?> response) {
        Converter<ResponseBody, ErrorResponse> converter = ServiceGenerator.retrofit()
                .responseBodyConverter(ErrorResponse.class, new Annotation[0]);

        ErrorResponse error;

        try {
            error = converter.convert(response.errorBody());
        } catch (Exception e) {
            return null;//new ErrorResponse();
        }

        return error;
    }
}

