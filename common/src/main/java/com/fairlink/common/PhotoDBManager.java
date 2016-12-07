package com.fairlink.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.fairlink.common.Application;
import com.fairlink.common.Logger;

public class PhotoDBManager {
	private DBHelper helper;
	private SQLiteDatabase db;
	private Logger logger = new Logger(this, "application");

	public PhotoDBManager(Context context) {
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
	}

	public void add(Collection<Application> applications) {
		db.beginTransaction();
		try {
			for (Application application : applications) {
				add(application);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void add(Application application) {
		try {
			db.execSQL("INSERT INTO photos VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] {application.getId(), application.getName(), 
					application.getDeveloper(),application.getComponentName(), 
					application.getType(), application.getCategory(), application.getDescription(), 
					application.getVersion(), application.getOrigin(), application.getIsUsing()});
		} catch (SQLException e) {
			logger.error("add [" + application.toString() + "] failed with error: " + e.getMessage());
		}
	}
	
	public void addPictureItem(String key, String value) {
		try {
			db.execSQL("INSERT INTO cachefile VALUES(?, ?)",
					new Object[] {key, value});
		} catch (SQLException e) {
			logger.error("add(String key, String value)");
		}
	}

	public void delete(Application application) {
		db.delete("application", "id = ?", new String[] { application.getId()
				.toString() });
	}
	
	public void deletePictureItem(String key){
		db.delete("cachefile", "key = ?", new String[] { key });
	}

	public void update(Application application) {
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put("name", application.getName());
			cv.put("developer", application.getDeveloper());
			cv.put("component_name", application.getComponentName());
			cv.put("type", application.getType());
			cv.put("category", application.getCategory());
			cv.put("description", application.getDescription());
			cv.put("version", application.getVersion());
			cv.put("origin", application.getOrigin());
			cv.put("is_using", application.getIsUsing());
			db.update("application", cv, "id = ?", new String[] { application.getId().toString() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public List<Application> queryApplications() {
		List<Application> applicationList = new ArrayList<Application>();
		Cursor c = queryTheCursor();
		while (c.moveToNext()) {
			Application application = new Application();
			application.setId(c.getInt(c.getColumnIndex("id")));
			application.setName(c.getString(c.getColumnIndex("name")));
			application
					.setDeveloper(c.getString(c.getColumnIndex("developer")));
			application.setComponentName(c.getString(c
					.getColumnIndex("component_name")));
			application.setType(c.getString(c.getColumnIndex("type")));
			application.setCategory(c.getString(c.getColumnIndex("category")));
			application.setDescription(c.getString(c
					.getColumnIndex("description")));
			application.setVersion(c.getString(c.getColumnIndex("version")));
			application.setOrigin(c.getString(c.getColumnIndex("origin")));
			application.setIsUsing(c.getInt(c.getColumnIndex("is_using")));
			applicationList.add(application);
		}
		c.close();
		return applicationList;
	}
	
	public HashMap<String,String> queryCacheFile() {
		HashMap<String,String> cachefile = new HashMap<String, String>();
		Cursor c = queryCacheCursor();
		while (c.moveToNext()) {
			cachefile.put(c.getString(c.getColumnIndex("key")), c.getString(c.getColumnIndex("value")));
		}
		c.close();
		return cachefile;
	}

	/**
	 * query all persons, return cursor
	 * 
	 * @return Cursor
	 */
	public Cursor queryTheCursor() {
		Cursor c = db.rawQuery("SELECT * FROM application", null);
		return c;
	}
	
	public Cursor queryCacheCursor() {
		Cursor c = db.rawQuery("SELECT * FROM cachefile", null);
		return c;
	}

	/**
	 * close database
	 */
	public void closeDB() {
		db.close();
	}
}
