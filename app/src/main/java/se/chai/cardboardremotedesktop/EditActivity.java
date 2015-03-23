package se.chai.cardboardremotedesktop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;


public class EditActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private ServerData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(true);

        AdLogic ads = new AdLogic();
        ads.loadAds(this);

        data = new ServerData();
        Intent intent = getIntent();
        data.id = intent.getStringExtra("id");
        data.name = intent.getStringExtra("name");
        ((EditText) findViewById(R.id.editTextName)).setText(data.name);
        data.password = intent.getStringExtra("password");
        ((EditText) findViewById(R.id.editTextPassword)).setText(data.password);
        data.host = intent.getStringExtra("host");
        ((EditText) findViewById(R.id.editTextHost)).setText(data.host);

        data.colormode = intent.getStringExtra("colormode");
        Spinner colorSpinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.colormode_types, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        colorSpinner.setAdapter(adapter);
        colorSpinner.setOnItemSelectedListener(this);
        if (data.colormode != null) {
            int pos = adapter.getPosition(data.colormode);
            colorSpinner.setSelection(pos, false);
        }
        data.viewonly = intent.getBooleanExtra("viewonly", false);
        CheckBox checkBox = (CheckBox) findViewById(R.id.viewCheckBox);
        checkBox.setChecked(data.viewonly);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            data.name = ((EditText) findViewById(R.id.editTextName)).getText().toString();
            data.host = ((EditText) findViewById(R.id.editTextHost)).getText().toString();
            data.username = "";
            data.password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();
            data.iconResource = R.drawable.vnc_button;
            data.viewonly = ((CheckBox) findViewById(R.id.viewCheckBox)).isChecked();
            ServerList.getServerList().add(data);

            super.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        data.colormode = (String) parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
