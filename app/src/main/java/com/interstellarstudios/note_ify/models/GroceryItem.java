package com.interstellarstudios.note_ify.models;

public class GroceryItem {

    private String item;

    public GroceryItem() {
        //empty constructor needed
    }

    public GroceryItem(String item) {

        this.item = item;
    }

    public String getItem() {
        return item;
    }
}
