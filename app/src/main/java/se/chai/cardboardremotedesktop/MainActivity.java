package se.chai.cardboardremotedesktop;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ServerList.getServerList().load(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        AdLogic ads = new AdLogic();
        ads.loadAds(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ServerListFragment())
                    .commit();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        ServerList.getServerList().save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ServerListFragment extends Fragment implements View.OnClickListener {

        private CardAdapter adapter;

        public ServerListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final FragmentActivity c = getActivity();
            final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.card_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(c);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new CardAdapter();
            recyclerView.setAdapter(adapter);

            ImageButton addButton = (ImageButton) rootView.findViewById(R.id.button);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                addButton.setBackgroundResource(R.drawable.fab);
                addButton.setTranslationZ(5);
            } else {
                addButton.setBackgroundResource(R.drawable.fab_shadow);
            }

            addButton.setOnClickListener(this);

            return rootView;
        }

        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(v.getContext(), EditActivity.class);
            v.getContext().startActivity(myIntent);
        }

    }
}
