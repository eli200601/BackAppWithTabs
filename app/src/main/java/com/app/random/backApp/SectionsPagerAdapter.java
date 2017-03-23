package com.app.random.backApp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.app.random.backApp.Fragments.CloudFragment;
import com.app.random.backApp.Fragments.DeviceAppsFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    SparseArray<Fragment> fragmentsMap = new SparseArray<>();

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentsMap.put(0, new DeviceAppsFragment());
        fragmentsMap.put(1, new CloudFragment());
        fragmentsMap.put(2, new DeviceAppsFragment());
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        Fragment fragment = fragmentsMap.get(position);
//        if (fragment == null) {
//            switch (position) {
//                case 0:
//                    fragment = new DeviceAppsFragment();
//                case 1:
//                    fragment = AppsListFragment.newInstance();
//                case 2:
//                    fragment = new AppsListFragment();
//            }
//            fragmentsMap.put(position,fragment);
//        }
        return fragment;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Device Apps";
            case 1:
                return "Cloud";
            case 2:
                return "TBD";
        }
        return null;
    }
}

