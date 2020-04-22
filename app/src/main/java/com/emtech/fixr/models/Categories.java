package com.emtech.fixr.models;

import com.emtech.fixr.data.database.Category;

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
