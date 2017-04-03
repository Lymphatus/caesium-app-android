package com.saerasoft.caesium;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


class CBuckets extends HashMap<Integer, CBucket> {
    private List<CBucket> sortedList;

    public int size() {
        return this.keySet().size();
    }

     long getTotalItemsSize() {
        long total = 0;
        for (Integer key : this.keySet()) {
            total += this.get(key).getItemsSize();
        }
        return total;
    }

    List<CBucket> sortList() {
        Collection<CBucket> collection = this.values();
        List<CBucket> list = new ArrayList<>(collection);
        Collections.sort(list);
        this.sortedList = list;
        return this.sortedList;
    }

    public void setSortedList(List<CBucket> sortedList) {
        this.sortedList = sortedList;
    }

    public List<CBucket> getSortedList() {
        return sortedList;
    }
}
