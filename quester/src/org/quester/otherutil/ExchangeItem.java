package org.quester.otherutil;

public class ExchangeItem {

    private String name;
    private int sellAverage;
    private int overallAverage;
    private int buyAverage;
    private int id;

    public ExchangeItem(String name, int id, int sellAverage, int overallAverage, int buyAverage) {
        this.name = name;
        this.id = id;
        this.sellAverage = sellAverage;
        this.overallAverage = overallAverage;
        this.buyAverage = buyAverage;
    }

    public String getName() {
        return name;
    }

    public int getSellAverage() {
        return sellAverage;
    }

    public int getOverallAverage() {
        return overallAverage;
    }

    public int getBuyAverage() {
        return buyAverage;
    }

    public int getId() {
        return id;
    }
}
