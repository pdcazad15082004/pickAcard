package com.example.pickacard;

public class IconUtil {
    public static String getBankIcon(String bank) {
        switch (bank.toLowerCase()) {
            case "sbi":
                return "sbi";
            case "hdfc":
                return "hdfc";
            case "icici":
                return "icici";
            case "kotak":
                return "kotak";
            case "axis":
                return "axis";
            case "canara":
                return "canara";
            case "rbl":
                return "rbl";
            case "indusind":
                return "indusind";
            default:
                return "default_logo";
        }
    }
}
