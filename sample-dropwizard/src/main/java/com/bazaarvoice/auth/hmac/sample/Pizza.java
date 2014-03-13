package com.bazaarvoice.auth.hmac.sample;

import java.util.List;

public class Pizza {
    private String size;
    private List<String> toppings;

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public void setToppings(List<String> toppings) {
        this.toppings = toppings;
    }

    public List<String> getToppings() {
        return toppings;
    }
}