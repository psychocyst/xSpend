package org.cystops.xSpend;

import java.util.ArrayList;

import org.cystops.xSpend.CollectionsActivity.CollectionPagerAdapter;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class HomeScreen extends FragmentActivity implements ActionBar.TabListener {
	
	private static Context context;
	private static final String AKS = "AKS";
	private final int EXPECTED_RESULT = 1;

	//Drawer UI Handlers
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	
	private boolean isDrawerOpenTabs;
	private boolean isDrawerOpenExpenses;
	
	private ListView listViewTabs;
	private ListView listViewExpenses;
	
	//Drawer programmatic handlers
	Summation summationTabs;
	Summation summationExpenses;
	
	ListItemsAdapter drawerAdapterTabs;
	ListItemsAdapter drawerAdapterExpenses;
	ArrayList<ArrayList<Object>> arrayListTabs = new ArrayList<ArrayList<Object>>();
	ArrayList<ArrayList<Object>> arrayListExpenses = new ArrayList<ArrayList<Object>>();
	
	//ViewPager handlers
	AppSectionsPagerAdapter appSectionsPagerAdapter;
	ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homescreen);
		context = this;
        
		listViewTabs = (ListView) findViewById(R.id.homescreen_listView_tabs_drawer);
		listViewExpenses = (ListView) findViewById(R.id.homescreen_listView_expenses_drawer);
		
		final ActionBar actionBar;
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
        //Changing ActionBar Title based on drawer opened/closed
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
        	public void onDrawerClosed(View view) {
        		actionBar.setTitle(R.string.app_name);
        		invalidateOptionsMenu();
            }
        	
        	public void onDrawerOpened(View drawerView) {
        		if(drawerView.getId() == R.id.homescreen_listView_tabs_drawer)
        			actionBar.setTitle("Tabs");
        		else if (drawerView.getId() == R.id.homescreen_listView_expenses_drawer)
        			actionBar.setTitle("Expenses");
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        
        //Managing ViewPager selection
        appSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.homescreen_viewPager);
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
        
		summationTabs = new Summation(context, "tabs");
		summationExpenses = new Summation(context, "expenses");
		
		arrayListTabs.clear();
		arrayListExpenses.clear();
		arrayListTabs = summationTabs.loadAllRecords();
		arrayListExpenses = summationExpenses.loadAllRecords();
		
		drawerAdapterTabs = new ListItemsAdapter(context, arrayListTabs);
		drawerAdapterExpenses = new ListItemsAdapter(context, arrayListExpenses);
		listViewTabs.setAdapter(drawerAdapterTabs);
		listViewExpenses.setAdapter(drawerAdapterExpenses);
		
		listViewTabs.setOnItemClickListener(new DrawerItemClickListener());
		listViewExpenses.setOnItemClickListener(new DrawerItemClickListener());
		
        if (savedInstanceState == null) {
            selectItem(0);
        }
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
                    return new HomeScreenFragment("tabs");
	        	case 1:
        			return new HomeScreenFragment("wallet");
        	    case 2:
                	return new HomeScreenFragment("expenses");
        	    default:
                    return new HomeScreenFragment("wallet");
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
		ListView listViewRecentRecords;
		TextView textViewTotalAmount;
	}
	
	@SuppressLint("ValidFragment")
	public static class HomeScreenFragment extends Fragment {

		Wallet wallet;
		ArrayList<ArrayList<Object>> recentRecords = new ArrayList<ArrayList<Object>>();
		String total = "0";
		ListItemsAdapter listItemsAdapter;
        ListView listView;
        TextView textView;
        View rootView;
        ViewHolder viewHolder;
        String type;
        
        public HomeScreenFragment() {
			//empty constructor created to accommodate below parameterized constructor
		}
        
        public HomeScreenFragment(String type) {
			super();
        	this.type = type;
		}

		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	setTableData(type);
        	rootView = inflater.inflate(R.layout.fragment_common, container, false);
        	viewHolder.textViewTotalAmount = (TextView) rootView.findViewById(R.id.fragment_textView_total_amount);
        	viewHolder.listViewRecentRecords = (ListView) rootView.findViewById(R.id.fragment_listView_recent_records);
        	viewHolder.listViewRecentRecords.setAdapter(listItemsAdapter);
        	viewHolder.textViewTotalAmount.setText(total);
        	if(type == "expenses") Log.i(AKS, ""+total);
    		rootView.setTag(viewHolder);
    		return rootView;
        }
        
        public void setTableData(String type) {
        	viewHolder = new ViewHolder();
        	wallet = new Wallet(context);
        	recentRecords = wallet.loadRecentTransactions(type);
        	if(type == "expenses") Log.i(AKS, ""+type);
			total = wallet.calculateTotal(type);
        	listItemsAdapter = new ListItemsAdapter(context, recentRecords);
        }
        
        @Override
        public void onStop () {
        	try{
       		wallet.closeDB();
        	} catch(Exception exception) {
        		Log.i(AKS, "SQL exception");
        	}
        	super.onStop();
        }
    }
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		//TODO need to fiddle here
		isDrawerOpenTabs = drawerLayout.isDrawerOpen(listViewTabs);
        isDrawerOpenExpenses = drawerLayout.isDrawerOpen(listViewExpenses);
        if(isDrawerOpenTabs|isDrawerOpenExpenses) {
//        	getActionBar().hide();
        	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        	menu.close();//.clear();
        } else {
        	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        	getActionBar().show();
        }
        return super.onPrepareOptionsMenu(menu);
    }
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	selectItem(position);
        }
    }
	
	private void selectItem(int position) {
		if(isDrawerOpenTabs) {
			listViewTabs.setItemChecked(position, true);
			drawerLayout.closeDrawer(listViewTabs);
			Intent intent = new Intent(context, CollectionsActivity.class);
			intent.putExtra("position", position);
			intent.putExtra("type", "iou");
			CollectionPagerAdapter.setDrawerAdapter(drawerAdapterTabs);
			startActivityForResult(intent, EXPECTED_RESULT);
		} else if(isDrawerOpenExpenses) {
			listViewExpenses.setItemChecked(position, true);
			drawerLayout.closeDrawer(listViewExpenses);
			Intent intent = new Intent(context, CollectionsActivity.class);
			intent.putExtra("position", position);
			intent.putExtra("type", "expense");
			CollectionPagerAdapter.setDrawerAdapter(drawerAdapterExpenses);
			startActivityForResult(intent, EXPECTED_RESULT);
		}
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_homescreen, menu);
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
    
	public void reloadAdapterData() {
		arrayListTabs.clear();
		arrayListExpenses.clear();
		arrayListTabs= summationTabs.loadAllRecords();
		arrayListExpenses = summationExpenses.loadAllRecords();
		drawerAdapterTabs.resetDataStore(arrayListTabs);
		drawerAdapterExpenses.resetDataStore(arrayListExpenses);
		drawerAdapterTabs.notifyDataSetChanged();
		drawerAdapterExpenses.notifyDataSetChanged();
	}
	
	@Override
	public boolean onOptionsItemSelected (final MenuItem menuItem){
		if(menuItem.getItemId() == R.id.Add_Tab|menuItem.getItemId() == R.id.Add_Exp) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle("Add New Record");
			alertDialogBuilder.setMessage("Record Name");
			final EditText editText = new EditText(context);
			InputFilter[] inputFilter = new InputFilter[1];
			inputFilter[0] = new InputFilter.LengthFilter(16);
			editText.setFilters(inputFilter);
			editText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_NORMAL);
			alertDialogBuilder.setView(editText);
			alertDialogBuilder.setCancelable(false)
				.setPositiveButton("Add",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						String recordName = editText.getText().toString();
						if(recordName.contains(" ") | recordName.length()==0 | !recordName.matches("[a-zA-Z0-9]*")) {
							showTableNameAlert();
						} else {
							//TODO change menu item names
							if(menuItem.getItemId() == R.id.Add_Tab) {
								recordName = "tabs_"+recordName;
								Ledger ledger = new Ledger(context, recordName, "tabs");
								ledger.closeDB();
								summationTabs.addRecord(recordName);
							} else if(menuItem.getItemId() == R.id.Add_Exp){
								recordName = "expenses_"+recordName;
								Ledger ledger = new Ledger(context, recordName, "expenses");
								ledger.closeDB();
								summationExpenses.addRecord(recordName);
							}
							reloadAdapterData();
						}
					}
				})
				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
		return true;
	}
    
    protected void showTableNameAlert() {
		// TODO check if modification is required
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle("enter a valid tab name");
		builder.setMessage("tab's name cannot be empty, contain spaces or special characters")
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //do things
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == EXPECTED_RESULT) {
			reloadAdapterData();
		}
	}
    
	@Override
    public void finish() {
    	summationTabs.closeDB();
    	summationExpenses.closeDB();
    	super.finish();
    }

}
