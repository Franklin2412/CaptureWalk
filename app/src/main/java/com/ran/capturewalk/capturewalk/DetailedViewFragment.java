package com.ran.capturewalk.capturewalk;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.ran.capturewalk.capturewalk.com.ran.capturewalk.capturewalk.model.DistanceDetails;
import java.util.ArrayList;

public class DetailedViewFragment extends Fragment {
    private AnyDBAdapter db;
    private ArrayList<DistanceDetails> distanceDetailsList;
    private MainActivity mCtx;
    private ListView mDistanceListView;

    public class CustomAdapter extends BaseAdapter {
        ArrayList<DistanceDetails> mList;

        public CustomAdapter(ArrayList<DistanceDetails> list) {
            this.mList = list;
        }

        public int getCount() {
            return this.mList.size();
        }

        public Object getItem(int arg0) {
            return this.mList.get(arg0);
        }

        public long getItemId(int arg0) {
            return (long) arg0;
        }

        public View getView(int position, View convertView, ViewGroup arg2) {
            if (convertView == null) {
                convertView = DetailedViewFragment.this.mCtx.getLayoutInflater().inflate(R.layout.row_view, null);
            }
            TextView date = (TextView) convertView.findViewById(R.id.date_textView);
            TextView time = (TextView) convertView.findViewById(R.id.time_textView);
            ((TextView) convertView.findViewById(R.id.distance_textView)).setText(((DistanceDetails) this.mList.get(position)).getDistance() + " metres");
            date.setText(((DistanceDetails) this.mList.get(position)).getDate());
            time.setText(((DistanceDetails) this.mList.get(position)).getTime());
            return convertView;
        }
    }

    public DetailedViewFragment() {
        this.distanceDetailsList = new ArrayList();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detailed_view, container, false);
        this.db = new AnyDBAdapter(this.mCtx.getApplicationContext());
        this.mDistanceListView = (ListView) view.findViewById(R.id.distanceList);
        this.db.open();
        Cursor c = this.db.SelectEntries();
        while (c.moveToNext()) {
            DistanceDetails distanceDetails = new DistanceDetails();
            distanceDetails.setDistance(c.getString(0));
            distanceDetails.setDate(c.getString(1));
            distanceDetails.setTime(c.getString(2));
            this.distanceDetailsList.add(distanceDetails);
        }
        this.mDistanceListView.setAdapter(new CustomAdapter(this.distanceDetailsList));
        return view;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mCtx = (MainActivity) activity;
    }
}
