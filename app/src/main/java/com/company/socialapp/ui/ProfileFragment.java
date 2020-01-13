package com.company.socialapp.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.socialapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileFragment extends Fragment {


    private NavController navController;
    private FirebaseAuth mAuth;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        mAuth = FirebaseAuth.getInstance();

        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new DemoPagerAdapter(getChildFragmentManager()));

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    class DemoPagerAdapter extends FragmentPagerAdapter {

        public DemoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return new PostsHomeFragment();
                case 1: return new PostsLikeFragment();
                case 2: return new PostsMyFragment();
                default: return new PostsHomeFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0: return "POSTS";
                case 1: return "LIKES";
                case 2: return "MY POSTS";
                default: return "POSTS";
            }
        }
    }
}
