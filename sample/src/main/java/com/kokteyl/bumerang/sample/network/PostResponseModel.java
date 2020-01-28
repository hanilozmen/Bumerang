package com.kokteyl.bumerang.sample.network;

import java.util.Locale;

public class PostResponseModel {
    String title;
    int year;
    RequestModel.SubItem subItem;
    int id;

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "title: %s, year: %s, id: %d, ", title, year, id) + (subItem == null ? "" : subItem.toString());
    }
}
