package com.app;


/**
 * Launcher Class nhằm vượt rào cản Module System của Java 11+
 * Khi chạy class này thay vì trực tiếp MainApp.java, JavaFX không yêu cầu phải 
 * cấu hình module-info.java gắt gao.
 */
public class Launcher {
    public static void main(String[] args) {
        com.app.ui.MainApp.main(args);
    }

}
