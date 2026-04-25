package com.shopping;

import com.shopping.gui.ShoppingGUI;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Force English locale globally so all numbers, dates, and currency
        // are rendered with Western numerals (0-9) regardless of OS locale.
        Locale.setDefault(Locale.US);

        System.out.println("====== SYSTEM MIGRATED TO DATABASE GUI ======\n");
        System.out.println("Launching new ShoppingGUI Application...");
        ShoppingGUI.main(args);
    }
}
