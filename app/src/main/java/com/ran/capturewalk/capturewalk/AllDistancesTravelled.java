package com.ran.capturewalk.capturewalk;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.ran.capturewalk.capturewalk.com.ran.capturewalk.capturewalk.model.DistanceDetails;
import java.util.ArrayList;

public class AllDistancesTravelled extends AppCompatActivity {
    private AnyDBAdapter db;
    private ArrayList<DistanceDetails> distanceDetailsList;
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
                convertView = AllDistancesTravelled.this.getLayoutInflater().inflate(R.layout.row_view, null);
            }
            TextView date = (TextView) convertView.findViewById(R.id.date_textView);
            TextView time = (TextView) convertView.findViewById(R.id.time_textView);
            ((TextView) convertView.findViewById(R.id.distance_textView)).setText(((DistanceDetails) this.mList.get(position)).getDistance() + " metres");
            date.setText(((DistanceDetails) this.mList.get(position)).getDate());
            time.setText(((DistanceDetails) this.mList.get(position)).getTime());
            return convertView;
        }
    }

    public AllDistancesTravelled() {
        this.distanceDetailsList = new ArrayList();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.alldistancestravelled);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_all_distance_travelled));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle((CharSequence) "All Distances Travelled");
        this.db = new AnyDBAdapter(getApplicationContext());
        this.mDistanceListView = (ListView) findViewById(R.id.distanceList);
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
    }
}
