package com.varteq.catslovers.api;

import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.ErrorResponse;

import retrofit2.Response;

public abstract class BaseParser<T> {

    public BaseParser(Response<BaseResponse<T>> response) {
        if (response.isSuccessful() && response.body() != null) {
            if (response.body().getSuccess() && response.body().getData() != null) {
                onSuccess(response.body().getData());
            } else {
                if (response.body().getData() != null && response.body().getData() instanceof ErrorData)
                    onFail(new ErrorResponse(((ErrorData) response.body().getData()).getMessage(),
                            ((ErrorData) response.body().getData()).getCode()));
                else onFail(null);
            }
        } else onFail(new ErrorResponse(response.message(), response.code()));
    }

    protected abstract void onSuccess(T data);

    protected abstract void onFail(ErrorResponse error);
}
