package com.app.yourrestaurantapp.models;

public class History {

    private String id;
    private String code;
    private String order_list;
    private String order_total;
    private String date_time;

    public History(String id, String code, String orderList, String orderTotal, String dateTime) {
        this.id = id;
        this.code = code;
        this.order_list = orderList;
        this.order_total = orderTotal;
        this.date_time = dateTime;
    }
}
