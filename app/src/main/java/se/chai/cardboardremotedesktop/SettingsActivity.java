package se.chai.cardboardremotedesktop;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.vrtoolkit.cardboard.CardboardDeviceParams;
import com.google.vrtoolkit.cardboard.CardboardView;


public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(true);

        AdLogic ads = new AdLogic();
        ads.loadAds(this);

        CardboardView view = new CardboardView(this);
        CardboardDeviceParams params = view.getCardboardDeviceParams();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();//
        editor.putInt("pref_lensSpacing", (int) (params.getInterLensDistance() * 1000));
        editor.putInt("pref_lensScreenDist", (int) (params.getScreenToLensDistance() * 1000));
        editor.putInt("pref_lensVertDist", (int) (params.getVerticalDistanceToLensCenter() * 1000));
        editor.commit();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.settingscontainer, new SettingsFragment())
                            //.replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CardboardView view = new CardboardView(this);
        CardboardDeviceParams params = view.getCardboardDeviceParams();
        float lensSpacing = PreferenceManager.getDefaultSharedPreferences(this).getInt("pref_lensSpacing",(int) (params.getInterLensDistance() * 1000)) / 1000f;
        float lensScreenDist = PreferenceManager.getDefaultSharedPreferences(this).getInt("pref_lensScreenDist",(int) (params.getScreenToLensDistance() * 1000)) / 1000f;
        float lensVertDist = PreferenceManager.getDefaultSharedPreferences(this).getInt("pref_lensVertDist",(int) (params.getVerticalDistanceToLensCenter() * 1000)) / 1000f;
        params = new CardboardDeviceParams(params);
        params.setInterLensDistance(lensSpacing);
        params.setScreenToLensDistance(lensScreenDist);
        params.setVerticalDistanceToLensCenter(lensVertDist);
        view.updateCardboardDeviceParams(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingsFragment extends PreferenceFragment {
//
//        public SettingsFragment() {
//        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_preferences, container, false);
//            return rootView;
//        }
    }
}
