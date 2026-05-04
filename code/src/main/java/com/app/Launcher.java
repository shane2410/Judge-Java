package com.app;

import com.app.ui.MainApp;

/**
 * Launcher Class nhằm vượt rào cản Module System của Java 11+
 * Khi chạy class này thay vì trực tiếp MainApp.java, JavaFX không yêu cầu phải 
 * cấu hình module-info.java gắt gao.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
