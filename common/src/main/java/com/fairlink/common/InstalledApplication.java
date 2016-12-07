package com.fairlink.common;

import android.graphics.drawable.Drawable;

import com.fairlink.common.Application;

public class InstalledApplication extends Application {
    private Drawable icon;
    
	public InstalledApplication() {}
	
	public InstalledApplication(Application app) {
		setId(app.getId());
		setName(app.getName());
		setVersion(app.getVersion());
		setDeveloper(app.getDeveloper());
		setComponentName(app.getComponentName());
		setType(app.getType());
		setCategory(app.getCategory());
		setDescription(app.getDescription());
		setOrigin(app.getOrigin());
		setIsUsing(app.getIsUsing());
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	} 
}
