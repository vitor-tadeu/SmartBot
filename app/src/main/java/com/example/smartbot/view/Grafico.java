package com.example.smartbot.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.smartbot.R;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class Grafico extends AppCompatActivity {
    private CombinedChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafico);

        mChart = findViewById(R.id.grafico);
        estilos();
        // draw bars behind lines
//        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
//                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
//        });

        legenda();
        XAxis eixoInferior = eixos();
        grafico(eixoInferior);
    }

    private void estilos() {
        mChart.getDescription().setText("");
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(true);
        mChart.setDrawBarShadow(true);
        mChart.setHighlightFullBarEnabled(false);
    }

    private void legenda() {
        Legend legend = mChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setFormSize(10);
        legend.setTextSize(12);
        legend.setXEntrySpace(10);
        legend.setTextColor(Color.BLACK);
    }

    private XAxis eixos() {
        YAxis eixoDireito = mChart.getAxisRight();
        eixoDireito.setAxisMinimum(0);
        eixoDireito.setDrawGridLines(true);
        eixoDireito.setTextColor(Color.BLACK);

        YAxis eixoEsquerdo = mChart.getAxisLeft();
        eixoEsquerdo.setAxisMinimum(0);
        eixoEsquerdo.setDrawGridLines(true);
        eixoEsquerdo.setTextColor(Color.BLACK);

        XAxis eixoInferior = mChart.getXAxis();
        eixoInferior.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        eixoInferior.setAxisMinimum(0);
        eixoInferior.setGranularity(1);
        eixoInferior.setAvoidFirstLastClipping(true);
        eixoInferior.setDrawAxisLine(true);
        eixoInferior.setDrawGridLines(true);
        eixoInferior.setTextSize(12);
        eixoInferior.setTextColor(Color.BLACK);
        eixoInferior.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return mMonths[(int) value % mMonths.length];
            }
        });
        return eixoInferior;
    }

    protected String[] mMonths = new String[]{
            "", "Anchieta", "Anhanguera", "Bandeirantes", "Castelo Branco", "Tamoios"
    };

    private void grafico(XAxis eixoInferior) {
        CombinedData data = new CombinedData();
        data.setData(generateLineData());
        data.setData(generateBarData());
        mChart.setData(data);
        eixoInferior.setAxisMaximum(data.getXMax() + 0.25f);
//        mChart.invalidate();
    }

    private LineData generateLineData() {
        LineData lineData = new LineData();

        ArrayList<Entry> entries = new ArrayList<>();
        entries = getLineEntriesData(entries);

        LineDataSet set = new LineDataSet(entries, "Velocidade");
        set.setColor(Color.RED);
//        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(0, 0, 156));
        set.setCircleRadius(5);
//        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(15);
        set.setValueTextColor(Color.rgb(0, 0, 156));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        lineData.addDataSet(set);
        return lineData;
    }

    private ArrayList<Entry> getLineEntriesData(ArrayList<Entry> entries) {
        entries.add(new Entry(1, 50));
        entries.add(new Entry(2, 60));
        entries.add(new Entry(3, 70));
        entries.add(new Entry(4, 80));
        entries.add(new Entry(5, 90));
        return entries;
    }

    private BarData generateBarData() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries = getBarEnteries(entries);

        BarDataSet set = new BarDataSet(entries, "Vias");
        set.setColor(Color.rgb(168, 168, 168));
//        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(15);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        float barWidth = 0.45f; // x2 dataset

        BarData barData = new BarData(set);
        barData.setBarWidth(barWidth);
        return barData;
    }

    private ArrayList<BarEntry> getBarEnteries(ArrayList<BarEntry> entries) {
        entries.add(new BarEntry(1, 60));
        entries.add(new BarEntry(2, 70));
        entries.add(new BarEntry(3, 80));
        entries.add(new BarEntry(4, 90));
        entries.add(new BarEntry(5, 100));
        return entries;
    }
}