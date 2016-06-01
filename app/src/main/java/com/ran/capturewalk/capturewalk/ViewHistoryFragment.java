package com.ran.capturewalk.capturewalk;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class ViewHistoryFragment extends Fragment {
    private MainActivity mCtx;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList;
        private final List<String> mFragmentTitleList;

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
            this.mFragmentList = new ArrayList();
            this.mFragmentTitleList = new ArrayList();
        }

        public Fragment getItem(int position) {
            return (Fragment) this.mFragmentList.get(position);
        }

        public int getCount() {
            return this.mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            this.mFragmentList.add(fragment);
            this.mFragmentTitleList.add(title);
        }

        public CharSequence getPageTitle(int position) {
            return (CharSequence) this.mFragmentTitleList.get(position);
        }
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_history, container, false);
        this.viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(this.viewPager);
        this.tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        this.tabLayout.setupWithViewPager(this.viewPager);
        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this.mCtx.getFragmentManager());
        adapter.addFragment(new DetailedViewFragment(), "DETAILED VIEW");
        adapter.addFragment(new GraphicalViewFragment(), "GRAPHICAL VIEW");
        viewPager.setAdapter(adapter);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mCtx = (MainActivity) activity;
    }
}
