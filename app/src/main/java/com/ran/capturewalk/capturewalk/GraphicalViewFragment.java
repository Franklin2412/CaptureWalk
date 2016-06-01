package com.ran.capturewalk.capturewalk;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

public class GraphicalViewFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphical_view, container, false);
        ((GraphView) view.findViewById(R.id.line_graph)).addSeries(new LineGraphSeries(new DataPoint[]{new DataPoint(0.0d, 1.0d), new DataPoint(1.0d, 5.0d), new DataPoint(2.0d, 3.0d), new DataPoint(3.0d, 2.0d), new DataPoint(4.0d, 6.0d)}));
        ((GraphView) view.findViewById(R.id.bar_graph)).addSeries(new BarGraphSeries(new DataPoint[]{new DataPoint(0.0d, 1.0d), new DataPoint(1.0d, 5.0d), new DataPoint(2.0d, 3.0d), new DataPoint(3.0d, 2.0d), new DataPoint(4.0d, 6.0d)}));
        ((GraphView) view.findViewById(R.id.points_graph)).addSeries(new PointsGraphSeries(new DataPoint[]{new DataPoint(0.0d, 1.0d), new DataPoint(1.0d, 5.0d), new DataPoint(2.0d, 3.0d), new DataPoint(3.0d, 2.0d), new DataPoint(4.0d, 6.0d)}));
        return view;
    }
}
