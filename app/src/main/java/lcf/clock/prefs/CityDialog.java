package lcf.clock.prefs;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.List;

import lcf.clock.R;
import lcf.weather.CitiesCallback;
import lcf.weather.City;
import lcf.weather.WeatherMain;

public class CityDialog extends PrefsDialog implements OnEditorActionListener,
		OnClickListener, OnItemClickListener {
	private TextView mSearchStatus;
	private ListView mCitiesList;
	private EditText mSearchLine;
	private Button mSearchButton;
	private CityListAdapter mCitiesListAdapter;
	private AppPreferences mAppPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.city_dialog);
		setTitle(R.string.cat1_city);
		applySize(R.id.dcityroot);

		mSearchStatus = (TextView) findViewById(R.id.searchStatus);
		mCitiesList = (ListView) findViewById(R.id.citieslist);
		mSearchLine = (EditText) findViewById(R.id.citysearch);

		mSearchLine.setOnEditorActionListener(this);
		mSearchButton = ((Button) findViewById(R.id.buttonSearch));
		mSearchButton.setOnClickListener(this);
		findViewById(R.id.closeButton)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});

		mCitiesListAdapter = new CityListAdapter(this);
		mCitiesList.setAdapter(mCitiesListAdapter);
		mCitiesList.setOnItemClickListener(this);

		mAppPreferences = new AppPreferences(this);
	}

	@Override
	public void onClick(View v) {
		search(false);
	}

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			if (event != null && event.isShiftPressed()) {
				return false;
			}
			search(false);
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAppPreferences.isCitySet()) {
			String c = mAppPreferences.getCityName();
			int p = c.lastIndexOf(",");
			if (p > 0) {
				mSearchLine.setText(c.substring(0, p));
			}
		} else {
			search(true);
		}
	}

	private void search(boolean byIp) {
		mCitiesListAdapter.clear();
		mCitiesList.setVisibility(View.GONE);
		mSearchStatus.setVisibility(View.VISIBLE);
		mSearchLine.setEnabled(false);
		mSearchButton.setEnabled(false);
		mSearchStatus.setText(R.string.search_process);
		CitiesCallback cc = new CitiesCallback() {
			@Override
			public void ready(List<City> result) {
				if (result == null || result.size() == 0) {
					if (getPattern() != null) {
						mSearchStatus.setText(R.string.notfound);
					} else {
						mSearchStatus.setText("");
					}
				} else {
					mSearchStatus.setText("");
					mSearchStatus.setVisibility(View.GONE);
					mCitiesList.setVisibility(View.VISIBLE);
					for (int i = 0; i < result.size(); i++) {
						mCitiesListAdapter.add(result.get(i));
					}
				}
				mSearchLine.setEnabled(true);
				mSearchButton.setEnabled(true);
			}
		};

		String apiKey = mAppPreferences.getApiKey();
		if (byIp) {
			WeatherMain.findNearestCitiesByCurrentIP(cc, apiKey);
		} else {
			String s = mSearchLine.getText().toString();
			if (s.length() == 0) {
				WeatherMain.findNearestCitiesByCurrentIP(cc, apiKey);
			} else {
				WeatherMain.findCities(s, cc, apiKey);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		City city = mCitiesListAdapter.getItem(position);
		mAppPreferences.setCity(CityListAdapter.getCityReadable(city, this), city.getId());
		finish();
	}
}
