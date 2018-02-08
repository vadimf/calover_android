package com.varteq.catslovers.utils;


import com.varteq.catslovers.R;

public interface Consts {

    String SAMPLE_CONFIG_FILE_NAME = "sample_config.json";

    int PREFERRED_IMAGE_SIZE_PREVIEW = ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size);
    int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);

    int CAT_MAX_YEARS_OLD = 20;
}