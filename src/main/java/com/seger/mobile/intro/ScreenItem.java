package com.seger.mobile.intro;

public class ScreenItem {
    String Title, Description;
    int ScreenImg;

    public ScreenItem(String title, String description, int screenImg){
        Title = title;
        Description =description;
        ScreenImg = screenImg;
    }

    public String getTitle() {
        return Title;
    }

    public String getDescription() {
        return Description;
    }

    public int getScreenImg() {
        return ScreenImg;
    }

}