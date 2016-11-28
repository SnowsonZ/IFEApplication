package snowson.ife.com.ifeapplication;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.fairlink.common.Logger;

public class BaseFragment extends Fragment {
	protected Logger logger = new Logger(this, "fragment");

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		logger.debug("onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logger.debug("onCreate");
	}

	@Override
	public void onStart() {
		super.onStart();
		logger.debug("onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		logger.debug("onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		logger.debug("onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		logger.debug("onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		logger.debug("onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		logger.debug("onDetach");
	}
}
