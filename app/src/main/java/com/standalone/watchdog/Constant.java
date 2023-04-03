package com.standalone.watchdog;

import java.util.HashMap;
import java.util.Map;

public class Constant {
    public static final String GREATER_THAN = "≥";
    public static final String LESS_THAN = "≤";
    public static final String APPLY = "Chấp nhận";
    public static final String CANCEL = "Hủy";
    public static final String EXIT = "Thoát";
    public static final String DIALOG_TITLE_DELETE = "Xóa cảnh báo";
    public static final String DIALOG_MSG_DELETE = "Bạn có muốn xóa cảnh báo này không?";
    public static final String NOTIFICATION_TITLE_NOTHING = "Đang theo dõi";
    public static final String NOTIFICATION_TITLE_WARNING = "Chạm mức cảnh báo";
    public static final String REQUIRE = "Bắt buộc";
    public static final String INVALID_SYMBOL = "Mã CP không tồn tại";
    public static final String NETWORK_ERROR = "Lỗi kết nối Internet";
    public static final Map<String, String> NOTIFICATION_COLS;
    static{
        NOTIFICATION_COLS=new HashMap<>();
        NOTIFICATION_COLS.put("symbol","Mã CP");
        NOTIFICATION_COLS.put("alarm","Cảnh báo");
        NOTIFICATION_COLS.put("market","Thị trường");
    }
}
