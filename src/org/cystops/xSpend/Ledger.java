package org.cystops.xSpend;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Ledger {

	private Context context;
	
	private SQLiteDatabase db;
	private String DB_NAME = "xspend";
	private final int DB_VERSION = 1;
	
	private String TABLE_NAME = "wallet";
	private String TABLE_TYPE = "tabs";
	private static final String KEY_ID = "id";
	private static final String KEY_AMOUNT = "amount";
	private static final String KEY_NAME = "name";
	private static final String KEY_NOTE = "note";
    private static final String KEY_TIME = "time";
    private static final String KEY_TYPE = "type";
    
    private static final String AKS = "AKS";
    
    public Ledger(Context context, String name, String type)
	{
		this.context = context;
		TABLE_NAME = name;
		TABLE_TYPE = type;
 
		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(this.context);
		this.db = helper.getWritableDatabase();
	}
    
    private class CustomSQLiteOpenHelper extends SQLiteOpenHelper
	{
		public CustomSQLiteOpenHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION);
		}
 
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// This string is used to create the database.  It should
			// be changed to suit your needs.
			String newTableQueryString = "CREATE TABLE " +
					TABLE_NAME + " ( " +
					KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
					KEY_AMOUNT + " INTEGER NOT NULL, " +
					KEY_NOTE + " TEXT DEFAULT ( 'misc' ), " +
					KEY_TIME + " DATETIME DEFAULT ( datetime( 'now' ) ), " +
					KEY_TYPE + " TEXT DEFAULT ( 'tabs' ) " +
					");";
			db.execSQL(newTableQueryString);
		}
 
 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			// NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
			// OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE.			
			Log.i(AKS, "onUpgrade");
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			String newTableQueryString = "CREATE TABLE IF NOT EXISTS " +
					TABLE_NAME + " ( " +
					KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
					KEY_AMOUNT + " INTEGER NOT NULL, " +
					KEY_NOTE + " TEXT DEFAULT ( 'misc' ), " +
					KEY_TIME + " DATETIME DEFAULT ( datetime( 'now' ) ), " +
					KEY_TYPE + " TEXT DEFAULT ( 'tabs' ) " +
					");";
			db.execSQL(newTableQueryString);
			
			String uponInsertIntoTableQuery = 
					"CREATE TRIGGER IF NOT EXISTS insertFrom" +
					TABLE_NAME + " AFTER INSERT ON " +
					TABLE_NAME + " FOR EACH ROW " +
					"BEGIN " +
						"INSERT INTO wallet ( " +
						KEY_AMOUNT + ", " + KEY_NAME + ", " +
						KEY_NOTE + ", " + KEY_TIME + ", " + KEY_TYPE + " ) " +
						"SELECT " + KEY_AMOUNT + ", '" +
						TABLE_NAME + "', " + KEY_NOTE + ", " +
						KEY_TIME + ", '" +
						TABLE_TYPE + "' " + " FROM " + TABLE_NAME + 
						" ORDER BY " + KEY_ID + 
						" DESC LIMIT 1; " +
						
						"UPDATE " + TABLE_TYPE +
						" SET amount =( " +
						"SELECT sum( " + KEY_AMOUNT + " ) " +
						"FROM " + TABLE_NAME + " ) " +
						"WHERE " + KEY_NAME + " = '" + TABLE_NAME + "'; " +
					"END;";
			db.execSQL(uponInsertIntoTableQuery);
		}
	}
    
    public int calculateTotal() {
    	Cursor cursor;
    	int total = 0;
    	try {
    		String getAmount = "SELECT SUM( " + KEY_AMOUNT + 
    			" ) FROM " + TABLE_NAME + ";";
		
    		cursor = db.rawQuery(getAmount, null);
    		cursor.moveToFirst();
    		total = cursor.getInt(0);
    		cursor.close();
    	}
		catch (SQLException e) {
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
		return total;
    }
    
    public ArrayList<ArrayList<Object>> loadRecentTransactions() {
    	ArrayList<ArrayList<Object>> recentRecords = new ArrayList<ArrayList<Object>>();
		Cursor cursor;
		
		try
			{
			String generateList = "SELECT " +
					KEY_NOTE + ", " +
					KEY_AMOUNT + " FROM " +
					TABLE_NAME + " ORDER BY " +
					KEY_ID + " DESC LIMIT 3;";
			
			cursor = db.rawQuery(generateList, null);
			
			cursor.moveToFirst();
			if (!cursor.isAfterLast())
			{
				do
				{
					ArrayList<Object> arrayList = new ArrayList<Object>();
					arrayList.add(cursor.getString(0));
					arrayList.add(cursor.getInt(1));
					recentRecords.add(arrayList);
				}
				while (cursor.moveToNext());
			}
				cursor.close();
			}
			catch (SQLException e) 
			{
				Log.e("DB ERROR", e.toString());
				e.printStackTrace();
			}
		return recentRecords;
	}
	
    public void addRecord (int amount, String note) {
    	String addRecordQuery = 
				"INSERT INTO " + TABLE_NAME +
				" ( " + KEY_AMOUNT + ", " +
				KEY_NOTE + " ) VALUES ( " +
				amount + ", '" +
				note + "' );";
				db.execSQL(addRecordQuery);
    }
    
    public String whoAreYou() {
    	return TABLE_NAME;
    }
    
    public void closeDB() {
    	db.close();
    }
    
}
