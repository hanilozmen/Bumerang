package com.kokteyl.bumerang.sample.network;


import java.util.List;

public class RequestModel {
    String title;
    int year;
    public SubItem subItem;


    public RequestModel(String title, int year, SubItem subItem) {
        this.title = title;
        this.year = year;
        this.subItem = subItem;
    }

    public static class SubItem {
        Boolean testBoolean;
        int testInt;
        List<String> items;

        public SubItem(List<String> items, int testInt, Boolean testBoolean) {
            this.testInt = testInt;
            this.testBoolean = testBoolean;
            this.items = items;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            for(String item: items) {
                sb.append("'").append(item).append("'").append(",");
            }
            sb.deleteCharAt(sb.length() -1);
            sb.append(" ]");
            return "testBoolean: " + testBoolean + " ,testInt: " + testInt + " ,items: " + sb.toString();
        }
    }


}
