package com.example.pickacard;

public class BankItem {

    private String bankName;
    private int bankLogo;  // ✅ Changed from String to int

    public BankItem() {
        // Default constructor required for Firebase
    }

    public BankItem(String bankName, int bankLogo) {
        this.bankName = bankName;
        this.bankLogo = bankLogo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public int getBankLogo() {
        return bankLogo;
    }

    public void setBankLogo(int bankLogo) {
        this.bankLogo = bankLogo;
    }
}
