package kz.incubator.myktybake.callofdutyteacher.docs_fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.adapters.ViewPagerAdapter;

public class MyLessonsFragment extends Fragment {
    View view;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_lessons, container, false);

        getActivity().setTitle(getString(R.string.my_lessons));
        viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new Semestr1Fragment(), getString(R.string.semestr1));
        adapter.addFragment(new Semestr2Fragment(), getString(R.string.semestr2));

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
    }
}
