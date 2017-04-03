package com.saerasoft.caesium;

/*
 * Created by lymphatus on 02/04/17.
 */

import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;

public interface MainActivityInterface {
    void showPieChart(PieChart pieChart);
    PieChart setPieChartData();
    void updateValues();
    int[] getIndicatorColors();
}
