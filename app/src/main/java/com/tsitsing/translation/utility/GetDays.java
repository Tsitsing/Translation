package com.tsitsing.translation.utility;

import java.util.ArrayList;
import java.util.List;

public class GetDays {
    public static List<String> getDays (float num) {
        List<String> list = new ArrayList<>();

        list.add("");
        list.add("");
        list.add("");
        list.add(String.valueOf(Math.ceil(num/200)).replace(".0", ""));//ceil：向上取整
        list.add(String.valueOf(Math.ceil(num/150)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/100)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/80)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/70)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/60)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/50)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/40)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/30)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/25)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/20)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/15)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/10)).replace(".0", ""));
        list.add(String.valueOf(Math.ceil(num/5)).replace(".0", ""));
        list.add("");
        list.add("");
        list.add("");

        return list;
    }
}
