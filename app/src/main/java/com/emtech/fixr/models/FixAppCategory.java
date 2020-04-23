package com.emtech.fixr.models;

public class FixAppCategory {
  private int category_id;
  private String category_name;
  private String category_description;

  public FixAppCategory(int category_id, String category_name, String category_description) {
    this.category_id = category_id;
    this.category_name = category_name;
    this.category_description = category_description;
  }

  public FixAppCategory(String category_name, String category_description) {
    this.category_name = category_name;
    this.category_description = category_description;
  }

  public int getCategory_id() {
    return category_id;
  }

  public void setCategory_id(int category_id) {
    this.category_id = category_id;
  }

  public String getCategory_name() {
    return category_name;
  }

  public void setCategory_name(String category_name) {
    this.category_name = category_name;
  }

  public String getCategory_description() {
    return category_description;
  }

  public void setCategory_description(String category_description) {
    this.category_description = category_description;
  }
}
