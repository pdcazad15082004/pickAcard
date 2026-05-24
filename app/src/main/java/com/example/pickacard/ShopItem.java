package com.example.pickacard;

public class ShopItem {
    private String name;
    private int logoResId;
    private String url;

    public ShopItem(String name, int logoResId, String url) {
        this.name = name;
        this.logoResId = logoResId;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public int getLogoResId() {
        return logoResId;
    }

    public String getUrl() {
        return url;
    }
}
