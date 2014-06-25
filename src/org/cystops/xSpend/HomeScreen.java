package org.cystops.xSpend;

import java.util.ArrayList;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeScreen extends FragmentActivity implements ActionBar.TabListener {
	
	private static Context context;
	private static final String AKS = "AKS";

	//Drawer UI Handlers
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	
	private boolean isDrawerOpenTabs;
	private boolean isDrawerOpenExpenses;
	
	private ListView listViewTabs;
	private ListView listViewExpenses;
	
	//ViewPager Details
	AppSectionsPagerAdapter appSectionsPagerAdapter;
	ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homescreen);
		context = this;
		
		final ActionBar actionBar;
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
		listViewTabs = (ListView) findViewById(R.id.listView_tabs_drawer);
		listViewExpenses = (ListView) findViewById(R.id.listView_expenses_drawer);
		
        //Changing ActionBar Title based on drawer opened/closed
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
        	public void onDrawerClosed(View view) {
        		actionBar.setTitle("Wallet");
        		invalidateOptionsMenu();
            }
        	
        	public void onDrawerOpened(View drawerView) {
        		if(drawerView.getId() == R.id.listView_tabs_drawer)
        			actionBar.setTitle("IOU Tabs");
        		else if (drawerView.getId() == R.id.listView_expenses_drawer)
        			actionBar.setTitle("Expense Tabs");
                invalidateOptionsMenu();
            }
        };
//        drawerLayout.setDrawerListener(drawerToggle);
        
        //Managing ViewPager selection
        appSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(appSectionsPagerAdapter);
        
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            	actionBar.setSelectedNavigationItem(position);
            }
        });
        
        for (int i = 0; i < appSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(appSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        viewPager.setCurrentItem(1);
	}
	
	@Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
	
	@Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }
	
	@Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
        	switch (i) {
        		case 0:
                    return new HomeScreenFragment(0);
	        	case 1:
        			return new HomeScreenFragment(1);
        	    case 2:
                	return new HomeScreenFragment(2);
        	    default:
                    return new HomeScreenFragment(1);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
            	return "Tabs";
            else if(position == 1)
            	return "Wallet";
            else if(position == 2)
            	return "Expenses";
            else
            	return "crap!";
        }
    }	
	
	static class ViewHolder {
		ListView recentRecords;
		TextView totalAmount;
	}
	
	@SuppressLint("ValidFragment")
	public static class HomeScreenFragment extends Fragment {

//		Wallet wallet;
		ArrayList<ArrayList<Object>> recentRecords = new ArrayList<ArrayList<Object>>();
		String total = "0";
		ListItemsAdapter listItemsAdapter;
        ListView listView;
        TextView textView;
        View rootView;
        ViewHolder viewHolder;
        int type;
        
        public HomeScreenFragment() {
			// TODO Auto-generated constructor stub
        	
		}
        
        public HomeScreenFragment(int i) {
			// TODO Auto-generated constructor stub
        	super();
        	type = i;
		}

		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	setTableData(type);
        	rootView = inflater.inflate(R.layout.fragment_common, container, false);
        	viewHolder.totalAmount = (TextView) rootView.findViewById(R.id.textView_total_amount);
        	viewHolder.recentRecords = (ListView) rootView.findViewById(R.id.listView_recent_records);
//        	viewHolder.recentRecords.setAdapter(listItemsAdapter);
        	viewHolder.totalAmount.setText(total);
    		rootView.setTag(viewHolder);
    		return rootView;
        }
        
        public void setTableData(int i) {
        	viewHolder = new ViewHolder();
//        	wallet = new Wallet(context);
        	switch (i) {
    			case 0:
//    				recentRecords = wallet.loadLastTenIOUs();
//    	        	total = wallet.calcTotalIOU();
    				total = "Tabs";
    				break;
    			case 1:
//    				recentRecords = wallet.loadLastTen();
//    	        	total = wallet.calcTotalWallet();
    				total = "Wallet";
    				break;
    			case 2:
//    				recentRecords = wallet.loadLastTenEXPs();
//    	        	total = wallet.calcTotalExpense();
    				total = "Expenses";
    				break;
    			default:
    				Toast.makeText(context, "crap! something's gone wrong!", Toast.LENGTH_SHORT).show();
        	}
//        	listItemsAdapter = new ListItemsAdapter(context, recentRecords);
        }
        
        @Override
        public void onStop () {
        	try{
 //       		wallet.closeDB();
        	} catch(Exception exception) {
        		Log.i(AKS, "SQL exception");
        	}
        	super.onStop();
        }
    }
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		isDrawerOpenTabs = drawerLayout.isDrawerOpen(listViewTabs);
        isDrawerOpenExpenses = drawerLayout.isDrawerOpen(listViewExpenses);
        if(isDrawerOpenTabs|isDrawerOpenExpenses) {
        	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        	menu.clear();
        } else {
        	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
        return super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

}
