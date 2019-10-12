package com.emtech.fixr.models;

import android.arch.lifecycle.LiveData;
import com.google.gson.annotations.SerializedName;

import com.emtech.fixr.data.database.Category;

import java.util.ArrayList;
import java.util.List;

public class Categories {
    private Category[] categories;

    public Categories() {

    }

    public Category[] getCategories() {
        return categories;
    }

    //LiveData<List<FixAppCategory>> getCategories();

    public void setMessages(Category[] categories) {
        this.categories = categories;
    }

}
