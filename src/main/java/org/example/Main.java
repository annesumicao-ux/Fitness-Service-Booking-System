package org.example;

import org.example.dao.DatabaseConnection;
import org.example.ui.MenuUI;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        MenuUI menu = new MenuUI();
        menu.start();
    }
}