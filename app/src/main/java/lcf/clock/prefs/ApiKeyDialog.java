package lcf.clock.prefs;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.List;

import lcf.clock.R;
import lcf.weather.CitiesCallback;
import lcf.weather.City;
import lcf.weather.WeatherMain;

public class ApiKeyDialog extends PrefsDialog implements OnEditorActionListener, OnClickListener {
	private static final String CITY_TO_VERIFY = "Gdansk";

	private TextView mSetStatus;
	private EditText mApiKeyEditText;
	private Button mSetApiKeyButton;
	private AppPreferences mAppPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.api_key_dialog);
		setTitle(R.string.preference_api_key);
		applySize(R.id.root);

		mSetStatus = (TextView) findViewById(R.id.setStatus);
		mApiKeyEditText = (EditText) findViewById(R.id.apiKeyEditText);

		mApiKeyEditText.setOnEditorActionListener(this);
		mSetApiKeyButton = (Button) findViewById(R.id.setApiKeyButton);
		mSetApiKeyButton.setOnClickListener(this);
		findViewById(R.id.closeButton)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});

		mAppPreferences = new AppPreferences(this);
	}

	@Override
	public void onClick(View v) {
		verifyApiKey();
	}

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			if (event != null && event.isShiftPressed()) {
				return false;
			}
			verifyApiKey();
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAppPreferences.isApiKeySet()) {
			String apiKey = mAppPreferences.getApiKey();
			mApiKeyEditText.setText(apiKey);
		}
	}

	private void verifyApiKey() {
		mSetStatus.setVisibility(View.VISIBLE);
		mApiKeyEditText.setEnabled(false);
		mSetApiKeyButton.setEnabled(false);
		mSetStatus.setText(R.string.verifying_api_key);

		final String apiKey = mApiKeyEditText.getText().toString();
		CitiesCallback cc = new CitiesCallback() {
			@Override
			public void ready(List<City> result) {
				if (result == null || result.size() == 0) {
					mSetStatus.setText(R.string.api_key_invalid);
				} else {
					mSetStatus.setText(R.string.api_key_ok);
					mAppPreferences.setApiKey(apiKey);
				}
				mApiKeyEditText.setEnabled(true);
				mSetApiKeyButton.setEnabled(true);
			}
		};

		WeatherMain.findCities(CITY_TO_VERIFY, cc, apiKey);
	}
}
