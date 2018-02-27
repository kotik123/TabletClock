package lcf.clock.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import lcf.clock.R;

/**
 * Facade for SharedPreferences to ease access to preferences.
 *
 * @author jacek
 * @since 09.04.17
 */
public class AppPreferences {
	private static final String VALUE_NOT_SET = " ";

	private final Context context;
	private final SharedPreferences sharedPreferences;

	public AppPreferences(Context context, SharedPreferences sharedPreferences) {
		this.context = context;
		this.sharedPreferences = sharedPreferences;
	}

	AppPreferences(Context context) {
		this(context, PreferenceManager.getDefaultSharedPreferences(context));
	}

	public String getApiKey() {
		return getStringPreference(R.string.key_api_key);
	}

	void setApiKey(String apiKey) {
		sharedPreferences.edit()
				.putString(context.getString(R.string.key_api_key), apiKey)
				.commit();
	}

	public boolean isApiKeySet() {
		return !"".equals(getApiKey().trim());
	}

	public int getCityId() {
		return sharedPreferences.getInt(context.getString(R.string.key_city_id), 0);
	}

	String getCityName() {
		return getStringPreference(R.string.key_city);
	}

	String getBatteryBarWidth() {
		return getStringPreference(R.string.key_progressbar_width);
	}

	String getBatteryBarHeight() {
		return getStringPreference(R.string.key_progressbar_height);
	}

	void setCity(String city, int id) {
		sharedPreferences.edit()
				.putString(context.getString(R.string.key_city), city)
				.putInt(context.getString(R.string.key_city_id), id)
				.commit();
	}

	boolean isCitySet() {
		return !"".equals(getCityName().trim());
	}

	private String getStringPreference(int key) {
		return sharedPreferences.getString(context.getString(key), "");
	}

	public boolean checkCityFirstTime() {
		if (getCityName().length() == 0) {
			setCity(VALUE_NOT_SET, 0);
			return true;
		}
		return false;
	}

	public boolean checkApiKeyFirstTime() {
		if (getApiKey().length() == 0) {
			setApiKey(VALUE_NOT_SET);
			return true;
		}
		return false;
	}
}
