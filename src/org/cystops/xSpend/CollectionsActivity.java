package org.cystops.xSpend;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class CollectionsActivity extends FragmentActivity implements OnClickListener {

	private static final String AKS = "AKS";
    private static Context context;
    
	String noteToAdd = "";
	String amountToAddText = "amount";
	Integer amountToAdd = 13;
	EditText editTextAmount;
	EditText editTextNote;
			
	CollectionPagerAdapter collectionPagerAdapter;
	static ViewPager viewPager;
	static HashMap<Integer, View> hashMap;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        context = this;
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
                
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        
        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager(), bundle.getString("type"));
        viewPager = (ViewPager) findViewById(R.id.collection_viewPager);
        viewPager.setAdapter(collectionPagerAdapter);
        viewPager.setCurrentItem(bundle.getInt("position"));

        editTextAmount = (EditText) findViewById(R.id.collection_editText_amount);
        editTextNote = (EditText) findViewById(R.id.collection_editText_note);
        View buttonAddRecord = findViewById(R.id.collection_button_add_record);
        buttonAddRecord.setOnClickListener(this);
        View buttonResetInput = findViewById(R.id.collection_button_reset_input);
        buttonResetInput.setOnClickListener(this);
    }
	
	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.collection_button_add_record) {
			checkAmount();
		} else if(view.getId() == R.id.collection_button_reset_input) {
			editTextAmount.setText(null);
			editTextNote.setText(null);
		}
	}

	private void checkAmount() {
		amountToAddText = editTextAmount.getText().toString();
		if(amountToAddText.length()==0 | amountToAddText.length()>7 | amountToAddText.contentEquals("-"))
			showAlertAmount();
		else {
			amountToAdd = Integer.parseInt(amountToAddText);
			checkNote();
		}
	}
	
	private void checkNote() {
		noteToAdd = editTextNote.getText().toString();
        if(noteToAdd.length()==0) {
        	noteToAdd = "misc";
        	runTransaction();
        }
        else if(noteToAdd.matches("[a-zA-Z0-9 ]*") && noteToAdd.length()<17)
        	runTransaction();
        else
        	showNoteAlertName();
	}
	
	private void runTransaction() {
	ObjectFragment objectFragment = (ObjectFragment) collectionPagerAdapter.getItem(viewPager.getCurrentItem());
		objectFragment.setTableData();
		objectFragment.insertInto(amountToAdd, noteToAdd);
		objectFragment.updateView();
		editTextAmount.setText(null);
		editTextNote.setText(null);
	}
	
	private void showAlertAmount() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Enter a valid amount")
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //do things
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showNoteAlertName() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle("enter a valid note");
		builder.setMessage("notes may not contain special characters or be greater than 16 characters")
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
	public void onBackPressed() {
		Intent intent = new Intent(this, HomeScreen.class);
        if (NavUtils.shouldUpRecreateTask(this, intent)) {
            TaskStackBuilder.from(this)
                    .addNextIntent(intent)
                    .startActivities();
            finish();
        } else {
            NavUtils.navigateUpTo(this, intent);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, HomeScreen.class);
                if (NavUtils.shouldUpRecreateTask(this, intent)) {
                    TaskStackBuilder.from(this)
                            .addNextIntent(intent)
                            .startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(this, intent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	public static class CollectionPagerAdapter extends FragmentStatePagerAdapter {
		
		private static ListItemsAdapter listItemsAdapter;
		private String type = "type";
    	
    	public CollectionPagerAdapter(FragmentManager fm, String type) {
            super(fm);
            hashMap = new HashMap<Integer, View>();
            this.type = type;
        }
    	
    	public static ListItemsAdapter getDrawerAdapter() {
			return listItemsAdapter;
		}

		public static void setDrawerAdapter(ListItemsAdapter drawerAdapter) {
			CollectionPagerAdapter.listItemsAdapter = drawerAdapter;
		}
		
		@Override
        public int getCount() {
            return listItemsAdapter.getCount();
        }
    	
		@Override
        public String getPageTitle(int position) {
			return listItemsAdapter.getItemLabel(position).replaceFirst("[a-zA-Z0-9]*_", "");
        }

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new ObjectFragment();
			String itemLabel = listItemsAdapter.getItemLabel(i);
			Bundle args = new Bundle();
			args.putInt("position", i);
            args.putString("itemName", itemLabel);
            args.putString("type", type);
            fragment.setArguments(args);
            return fragment;
		}
	}
	
	public static class ObjectFragment extends Fragment {
    	
		Ledger ledger;
        Integer recordTotal = 0;
        String recordName = "misc";
        String recordType = "type";
        Integer position = 0;
                    
		ArrayList<ArrayList<Object>> recentRecords = new ArrayList<ArrayList<Object>>();
        ListItemsAdapter listItemsAdapter;
        ListView listView;
        View rootView;
        
        ViewHolder viewHolder;
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	setTableData();
        	rootView = inflater.inflate(R.layout.collections_object, container, false);
        	
        	viewHolder.textViewRecordName = (TextView) rootView.findViewById(R.id.collection_object_textView_record_name);
			viewHolder.textViewRecordTotal = (TextView) rootView.findViewById(R.id.collection_object_textView_record_total);
			viewHolder.listViewRecentRecords = (ListView) rootView.findViewById(R.id.collection_object_listView_recent_records);
    		viewHolder.textViewRecordName.setText(recordName.replaceFirst("[a-zA-Z0-9]*_", ""));
    		viewHolder.textViewRecordTotal.setText(recordTotal.toString());
    		viewHolder.listViewRecentRecords.setAdapter(listItemsAdapter);
    		rootView.setTag(viewHolder);
    		hashMap.put(position, rootView);
        	return rootView;
        }
        
        public void setTableData() {
        	viewHolder = new ViewHolder();
        	Bundle args = getArguments();
        	position = args.getInt("position");
//        	tableName = args.getString("itemName").replaceFirst("[a-zA-Z0-9]*_", "");
        	recordName = args.getString("itemName");
        	recordType = args.getString("type");
        	ledger = new Ledger(context, recordName, recordType);
        	recordTotal = ledger.calculateTotal();
        	recentRecords = ledger.loadRecentTransactions();
            listItemsAdapter = new ListItemsAdapter(context, recentRecords);
        }
        
        public void insertInto(Integer amount, String note) {
			ledger.addRecord(amount, note);
			Log.i(AKS, ""+amount+" "+note);
			reloadRecentRecords();
		}
        
        public void reloadRecentRecords() {
        	recordTotal = ledger.calculateTotal();
        	recentRecords.clear();
        	recentRecords = ledger.loadRecentTransactions();
        	listItemsAdapter.resetDataStore(recentRecords);
        	listItemsAdapter.notifyDataSetChanged();
        }
        
        public void updateView() {
        	rootView = (View) hashMap.get(position);
        	viewHolder = (ViewHolder) rootView.getTag();
        	viewHolder.textViewRecordName.setText(recordName.replaceFirst("[a-zA-Z0-9]*_", ""));
    		viewHolder.textViewRecordTotal.setText(recordTotal.toString());
    		viewHolder.listViewRecentRecords.setAdapter(listItemsAdapter);
    		rootView.requestFocus();
    	}
        
        @Override
        public void onStop () {
        	try{
        		ledger.closeDB();
        	} catch(Exception exception) {
        		Log.i(AKS, "SQL exception");
        	}
        	super.onStop();
        }
	}

	static class ViewHolder {
		ListView listViewRecentRecords;
		TextView textViewRecordTotal;
		TextView textViewRecordName;
	}
	
	@Override
	public void finish() {
		setResult(RESULT_OK);
		super.finish();
	}
	
}
