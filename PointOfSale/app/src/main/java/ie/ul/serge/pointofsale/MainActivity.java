package ie.ul.serge.pointofsale;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private TextView mNameTextView, mQuantityTextView, mDateTextView;
    private Item mCurrentItem, mDeletedItem;
    private ArrayList<Item> mItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNameTextView = findViewById(R.id.name_text);
        mQuantityTextView = findViewById(R.id.quantity_text);
        mDateTextView = findViewById(R.id.date_text);
        mItems.add(new Item("boots", 20, new GregorianCalendar()));
        mItems.add(new Item("shorts", 2, new GregorianCalendar()));
        mItems.add(new Item("sandals", 5, new GregorianCalendar()));
        mItems.add(new Item("shades", 8, new GregorianCalendar()));
        registerForContextMenu(mNameTextView);


        //boilerplate content
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertItem(false);


            }
        });
    }

    private void insertItem(final boolean isEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("My Title");
        //builder.setMessage("Hello World My Message!");
        //builder.setPositiveButton("OK",null); -- but you can make dialogues from xml.
        // builder.setTitle(R.string.add_item);
        View view = getLayoutInflater().inflate(R.layout.dialog_add, null, false);
        builder.setView(view);
        //get values from the input fields and store them. They have to be final as they will be used in the inner class.
        final EditText nameEditText = view.findViewById(R.id.edit_name);
        final EditText quantityEditText = view.findViewById(R.id.edit_quantity);
        final CalendarView deliveryDateView = view.findViewById(R.id.calendar_view);
        final GregorianCalendar callendar = new GregorianCalendar();

        if(isEdit){
            nameEditText.setText(mCurrentItem.getName());
            quantityEditText.setText(""+mCurrentItem.getQuantity());
            deliveryDateView.setDate(mCurrentItem.getDeliveryDateTime());
        }

        deliveryDateView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                callendar.set(year, month, dayOfMonth);
            }
        });


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEditText.getText().toString();
                int quantity = Integer.parseInt(quantityEditText.getText().toString());




                if (isEdit){
                    mCurrentItem.setName(name);
                    mCurrentItem.setQuantity(quantity);
                    mCurrentItem.setDeliveryDate(callendar);

                }else {
                    mCurrentItem = new Item(name, quantity, callendar);
                    mItems.add(mCurrentItem);
                }
                showCurrentItem();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.create().show();

    }

    private void showCurrentItem() {
        mNameTextView.setText(mCurrentItem.getName());
        mQuantityTextView.setText(getString(R.string.quantity_format, mCurrentItem.getQuantity()));
        mDateTextView.setText(getString(R.string.date_format, mCurrentItem.getDeliveryDateString()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //boolerplate code!
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_reset:
                mDeletedItem = mCurrentItem;
                mCurrentItem = new Item();
                showCurrentItem();

                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Item cleared", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCurrentItem = mDeletedItem;
                        showCurrentItem();
                    }
                });

                snackbar.show();
                return true;

            case R.id.action_search:
                showSearchDialog();

                return true;

            case R.id.action_settings:
                startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));

                return true;
            case R.id.action_clear_all:

                clearAllItems();

                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_context,menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                insertItem(true);
                return true;
            case R.id.action_remove:
                mItems.remove(mCurrentItem);
                mCurrentItem = new Item();
                showCurrentItem();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void clearAllItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove_items_dialogue_title);
        builder.setMessage(R.string.remove_items_message);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mItems.clear();
                mCurrentItem = new Item();
                showCurrentItem();
            }
        });

        builder.create().show();
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_item_from_list);
        builder.setItems(getNames(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentItem = mItems.get(which);
                showCurrentItem();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.create().show();
    }

    private String[] getNames() {
        String[] names = new String[mItems.size()];

        for (int i = 0; i < mItems.size(); i++) {
            names[i] = mItems.get(i).getName();
        }

        return names;
    }
}
