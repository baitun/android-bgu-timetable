package ru.savinyurii.timetable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import ru.savinyurii.timetable.R;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TimeTableActivity extends FragmentActivity implements ActionBar.TabListener {
    static final int NUM_ITEMS = 7;
    static final int NUM_ITEMS_IN_LIST = 4;

    MyPagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    static String[] weekdays;
    SharedPreferences sharedPref;
    
    static String[][] tmpArray=new String [NUM_ITEMS][NUM_ITEMS_IN_LIST];

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_pager);
        
        // TODO remove it
        for(int i=0;i<NUM_ITEMS;i++){
        	for(int j=0;j<NUM_ITEMS_IN_LIST;j++){
        		tmpArray[i][j]="Пара № "+j;
        	}
        }
        
        // Getting info from preferences
        sharedPref=getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String pref_group_id=sharedPref.getString("group_id", null);
        Log.i("myLog","curren group="+pref_group_id);
        if(pref_group_id==null){ // if app running first time
        	startActivity(new Intent(this, ChooseFacultyAndGroup.class));
        }
        
        // Open file and convert into String
        /*StringBuilder theStringBuilder = new StringBuilder();        
	    try{
	    	File file = new File(getFilesDir(), URLs.FILE_TIMETABLE);
	        BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) // Read in the data from the Buffer until nothing is left
			{
				theStringBuilder.append(line);
			}
			reader.close();
	    } catch (FileNotFoundException e) {
			Log.e("myLog", "file not found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("myLog", "Error while reading file");
			e.printStackTrace();
		}
	    String scheduleJsonString=theStringBuilder.toString();*/
	    //end opening and converting
	    //Log.i("myLog", "schedule from file: "+scheduleJsonString);
	    	        
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        
        final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });
        
        weekdays = getResources().getStringArray(R.array.weekdays);
        
        for(int i=0;i<weekdays.length; i++){
        	actionBar.addTab(actionBar.newTab().setText(weekdays[i]).setTabListener(this));
        }	
    }

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		
	}
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}
	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	}
    
    
	/**
	 * PagerAdapter for PagerView
	 */
	public static class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
        	// TODO
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            return DayFragment.newInstance(position);
        }
    }

	
	/**
	 * The Fragment which represent one day of timetable with list of lessons
	 */
    public static class DayFragment extends Fragment implements OnItemClickListener {
    	int mNum;
        ListView lvList;

        /**
         * Create a new instance of CountingFragment, providing "num" as an argument.
         */
        static DayFragment newInstance(int num) {
            DayFragment f = new DayFragment();

            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         *  This is the most important method of this class. All others methods can be moved here.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_one_day_list, container, false);
            
            lvList = (ListView) v.findViewById(R.id.list);		
    		lvList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            
            TextView tv = (TextView) v.findViewById(R.id.daytitle);
            tv.setText(weekdays[mNum]);
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // TODO change ArrayAdapter to SimpleAdapter
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_lesson, R.id.tvLesson, tmpArray[mNum]);
            lvList.setAdapter(adapter);
            lvList.setOnItemClickListener(this);
        }
        
		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			Log.i("myLog", "Item clicked: " + id);
		}
    }
}