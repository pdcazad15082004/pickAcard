package com.example.pickacard;

public class CardModel {
    private String cardId;
    public String number;
    public String validity;
    public String holder;
    public String type;
    public String logo;      // e.g. "hdfclogo"
    public String network;
    public String bank;

    public String limit;

    public CardModel() {
        // Required empty constructor for Firebase
    }

    public CardModel(String cardId, String number, String validity, String holder, String type, String logo, String network, String bank,String limit) {
        this.cardId = cardId;
        this.number = number;
        this.validity = validity;
        this.holder = holder;
        this.type = type;
        this.logo = logo;
        this.network = network;
        this.bank = bank;
        this.limit = limit;

    }


    public String getCardId() {
        return cardId;
    }

    public String getCardName() {
        return bank;
    }


    public String getCardLogo() {

        return logo;
    }

    public String getLimit() {
        return limit;
    }



    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public void setLimit(String limit) {this.limit = limit;
    }



}
