package snowson.ife.com.ifeapplication;

import android.os.Bundle;

import com.fairlink.common.Analytics;
import com.fairlink.common.AnalyticsType;

import snowson.ife.com.ifeapplication.application.IFEApplication;

public class ThirdpartyWebActivity extends BaseActivity {
	private WebFragment webFragment;
	private int parentId = -1;
	private int resourceId = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thirdparty_web);
		
		parentId = getIntent().getIntExtra("parentId", -1);
		resourceId = getIntent().getIntExtra("resourceId", -1);
		webFragment = (WebFragment) getFragmentManager().findFragmentById(R.id.web);
		if(parentId != -1) {
		    webFragment.setParentId(parentId);
		}
	}
	
	
	@Override
	protected void onDestroy() {
		Analytics.logEvent(IFEApplication.getInstance().getApplicationContext(), AnalyticsType.getOperationDynamic(2),
                AnalyticsType.ORIGIN_DETAIL, AnalyticsType.getAnalyticsData(resourceId, AnalyticsType.RESOURCE_TYPE_OTHER));
	    super.onDestroy();
	}
	
    
    @Override
    public void onBackPressed() {
        if(webFragment == null || !webFragment.goBack()) {
            super.onBackPressed();
        }
    }
}
