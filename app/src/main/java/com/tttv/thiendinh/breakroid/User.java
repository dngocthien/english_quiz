package com.tttv.thiendinh.breakroid;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public static final String SAVE_MAIL = "my_mail";
    public static final String SAVE_NAME = "my_name";
    public static final String SAVE_PHONE = "my_phone";
    public static final String SAVE_AVATAR = "my_avatar";
    public static final String STARS = "stars";

    private String my_mail, my_name, my_phone, my_avatar;
    private int stars;

    public User() {
    }

    public String getMy_mail() {
        return my_mail;
    }

    public void setMy_mail(String my_mail) {
        this.my_mail = my_mail;
    }

    public String getMy_name() {
        return my_name;
    }

    public void setMy_name(String my_name) {
        this.my_name = my_name;
    }

    public String getMy_phone() {
        return my_phone;
    }

    public void setMy_phone(String my_phone) {
        this.my_phone = my_phone;
    }

    public String getMy_avatar() {
        return my_avatar;
    }

    public void setMy_avatar(String my_avatar) {
        this.my_avatar = my_avatar;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
