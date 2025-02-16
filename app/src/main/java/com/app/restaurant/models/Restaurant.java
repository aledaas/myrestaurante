package com.app.restaurant.models;

public class Restaurant {
    private String restoId;
    private String restoTitle;
    private String restoImage;
    private String restoDescription;
    private String restoAddress;
    private String restoCelular;
    private String restoTelfijo;
    private String restoWhatshap;
    private String restoDate;

    // Constructor vacío (obligatorio para JSON parsing)
    public Restaurant() {}

    // Constructor con todos los campos
    public Restaurant(String restoId, String restoTitle, String restoImage, String restoDescription,
                      String restoAddress, String restoCelular, String restoTelfijo, String restoWhatshap, String restoDate) {
        this.restoId = restoId;
        this.restoTitle = restoTitle;
        this.restoImage = restoImage;
        this.restoDescription = restoDescription;
        this.restoAddress = restoAddress;
        this.restoCelular = restoCelular;
        this.restoTelfijo = restoTelfijo;
        this.restoWhatshap = restoWhatshap;
        this.restoDate = restoDate;
    }

    // Getters y Setters
    public String getRestoId() {
        return restoId;
    }

    public void setRestoId(String restoId) {
        this.restoId = restoId;
    }

    public String getRestoTitle() {
        return restoTitle;
    }

    public void setRestoTitle(String restoTitle) {
        this.restoTitle = restoTitle;
    }

    public String getRestoImage() {
        return restoImage;
    }

    public void setRestoImage(String restoImage) {
        this.restoImage = restoImage;
    }

    public String getRestoDescription() {
        return restoDescription;
    }

    public void setRestoDescription(String restoDescription) {
        this.restoDescription = restoDescription;
    }

    public String getRestoAddress() {
        return restoAddress;
    }

    public void setRestoAddress(String restoAddress) {
        this.restoAddress = restoAddress;
    }

    public String getRestoCelular() {
        return restoCelular;
    }

    public void setRestoCelular(String restoCelular) {
        this.restoCelular = restoCelular;
    }

    public String getRestoTelfijo() {
        return restoTelfijo;
    }

    public void setRestoTelfijo(String restoTelfijo) {
        this.restoTelfijo = restoTelfijo;
    }

    public String getRestoWhatshap() {
        return restoWhatshap;
    }

    public void setRestoWhatshap(String restoWhatshap) {
        this.restoWhatshap = restoWhatshap;
    }

    public String getRestoDate() {
        return restoDate;
    }

    public void setRestoDate(String restoDate) {
        this.restoDate = restoDate;
    }
}