package com.emtech.fixr.models;

import android.net.Uri;

public class UploadImage {
  private int id;
  private Uri imagePath;
  private String image;
  private String title;
  private int resImg;
  private boolean isSelected;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Uri getImagePath() {
    return imagePath;
  }

  public void setImagePath(Uri imagePath) {
    this.imagePath = imagePath;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getResImg() {
    return resImg;
  }

  public void setResImg(int resImg) {
    this.resImg = resImg;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
  }
}
