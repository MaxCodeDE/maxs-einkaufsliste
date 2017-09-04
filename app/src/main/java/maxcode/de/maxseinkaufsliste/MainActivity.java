package maxcode.de.maxseinkaufsliste;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> items;
    ArrayList<String> selectedItems;
    EditText editText;
    ArrayAdapter<String> arrayAdapter;
    ListView chl;

    public static final String PREFS_NAME = "MaxsEinkaufslisteFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedItems = new ArrayList<String>();
        chl = (ListView) findViewById(R.id.clickable_item_list);
        chl.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        items = new ArrayList<>();
        items.clear();

        // Array Adapter setup
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.rowlayout, R.id.item_row, items);
        chl.setAdapter(arrayAdapter);
        loadItems();
        chl.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Makiertes Item
                String selectedItem = ((TextView) view).getText().toString();
                if (itemIsSelected(selectedItem)) {
                    // Änderungen speichern
                    setItem(selectedItem, false);
                    loadItems();
                } else {
                    // Änderungen speichern
                    setItem(selectedItem, true);
                    loadItems();
                }
            }

        });

        editText = (EditText)findViewById(R.id.item_input);
        Button addItemButton = (Button)findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItem = editText.getText().toString();
                if (newItem.equalsIgnoreCase("") || newItem == null) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("Fehler!");
                    alert.setMessage("Das Eingabefeld ist leer.");
                    alert.setCancelable(false);
                    alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alert.create().show();
                } else {
                    // Eingabefeld leeren
                    editText.getText().clear();
                    // Adapter neu laden -> Änderungen werden angezeigt
                    arrayAdapter.notifyDataSetChanged();
                    // Änderungen speichern
                    setItem(newItem, false);
                    // Item hinzufügen
                    loadItems();

                    Toast toast = Toast.makeText(getApplicationContext(), newItem + " hinzugefügt!", Toast.LENGTH_LONG);
                    toast.show();
                }

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Löschen Icon anzeigen
        getMenuInflater().inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.deleteAllSelectedItemsButton:
                for (int i = 0; i < items.size(); i++) {
                    // GEspeicherte Items laden und in Array Speichern
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    Map<String, ?> allEntries = settings.getAll();
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        if (Boolean.valueOf(entry.getValue().toString())) {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.remove(entry.getKey());
                            editor.apply();
                        }
                    }
                }
                loadItems();
                arrayAdapter.notifyDataSetChanged();
                //Toast toast = Toast.makeText(getApplicationContext(), "Alle makierten gelöscht!", Toast.LENGTH_LONG);
                //toast.show();
                return true;
            case R.id.deleteAllItemsButton:
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Alle löschen?");
                alert.setMessage("Sicher das alle Einträge gelöscht werden sollen?");
                alert.setCancelable(true);
                alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Items löschen
                        items.clear();
                        selectedItems.clear();
                        // Eingabefeld leeren
                        editText.getText().clear();
                        // Items aus DB löschen
                        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, getApplicationContext().MODE_PRIVATE);
                        settings.edit().clear().commit();
                        // Adapter neu laden -> Änderungen werden angezeigt
                        arrayAdapter.notifyDataSetChanged();
                        // Info ausgeben
                        Toast toast = Toast.makeText(getApplicationContext(), "Liste geleert!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });

                alert.setNegativeButton( "Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert11 = alert.create();
                alert11.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void setItem(String key, Boolean value) {
        // Änderungen speichern
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value.toString());
        editor.commit();
    }

    public Boolean itemIsSelected(String key) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Map<String, ?> allEntries = settings.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey())) {
                return Boolean.valueOf(entry.getValue().toString());
            }
        }
        return false;
    }

    public void loadItems() {
        items.clear();
        // GEspeicherte Items laden und in Array Speichern
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Map<String, ?> allEntries = settings.getAll();
        int i = 0;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            items.add(entry.getKey());
            chl.setItemChecked(i, Boolean.valueOf(entry.getValue().toString()));
            i++;
        }
    }



}
