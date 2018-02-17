package com.squidzoo.quamcrawler;

public class Stock {

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

    public float getPe() {
        return pe;
    }

    public void setPe(float pe) {
        this.pe = pe;
    }

    public float getPb() {
        return pb;
    }

    public void setPb(float pb) {
        this.pb = pb;
    }

    public float getYield() {
        return yield;
    }

    public void setYield(float yield) {
        this.yield = yield;
    }

    private int code = 0;
    private String name = "N/A";
    private String marketCap = "N/A";
    private float pe = 0.0F;
    private float pb = 0.0F;
    private float yield = 0.0F;

    @Override
    public String toString() {
        return "Stock: ("+ getCode() + ", " + getName() + ", " + getMarketCap() + ", " + getPe() + ", " + getPb() + ", " +  getYield() +")";
    }
}
