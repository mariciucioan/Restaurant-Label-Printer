package com.caramelpanda.lacezarprinter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.RawPrintable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;

public class MainActivity extends AppCompatActivity implements PrintingCallback {
    public static final String DATE_FORMAT_NOW = "dd-MM-yyyy HH:mm:ss";

    String[] Categories = { "Meat", "Sauces", "Cheese", "Vegetables", "Dough", "Ice cream", "Other" };
    int[] Colors = { R.color.colorMeat, R.color.colorSauces, R.color.colorCheese, R.color.colorVegetables,
            R.color.colorDough, R.color.colorIceCream, R.color.colorOther };
    int[] Icons = { R.drawable.meat, R.drawable.sauces, R.drawable.cheese, R.drawable.vegetables, R.drawable.dough,
    R.drawable.icecream, R.drawable.other };

    List<String> Products = new LinkedList<String>();

    ExpandingList expandingList;
    Activity activity;

    Printing printing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activity = this;
        Load();

        expandingList = (ExpandingList) findViewById(R.id.expanding_list_main);
        createItems();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(printing != null) {
            printing.setPrintingCallback(this);

            if(!Printooth.INSTANCE.hasPairedPrinter()) {
                Printooth.INSTANCE.removeCurrentPrinter();
            } else {
                startActivityForResult(new Intent(this, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
            }
        }
    }

    void PrintText(String text) {
        ArrayList<Printable> printables = new ArrayList<Printable>();
        printables.add(new RawPrintable.Builder(new byte[]{27, 100, 4}).build());

        printables.add(new TextPrintable.Builder()
        .setText(text)
        .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC850())
        .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
        .setNewLinesAfter(1)
        .build());

        printing.print(printables);
    }

    @Override
    public void connectingWithPrinter() {
        Toast.makeText(this, "Connecting to printer", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectionFailed(String s) {
        Toast.makeText(this, "Connection failed: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String s) {
        Toast.makeText(this, "Error: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessage(String s) {
        Toast.makeText(this, "Message: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void printingOrderSentSuccessfully() {
        Toast.makeText(this, "Successfully printed.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            initPrinting();
        }
    }

    private void initPrinting() {
        if(!Printooth.INSTANCE.hasPairedPrinter()) {
            printing = Printooth.INSTANCE.printer();
        }

        if(printing != null) {
            printing.setPrintingCallback(this);
        }
    }

    private void createItems() {
        int j=0;
        for (String cat : Categories) {
            int i = 0;
            String[] products = new String[22];
            for (String prod : Products) {
                if (prod.contains("(" + cat + ")")) {
                    products[i++] = prod;
                }
            }
            addItem(cat, products, i, Colors[j], Icons[j]);
            j++;
        }
    }

    private void addItem(final String title, String[] subItems, int length, int ColorRes, int IconRes) {
        final ExpandingItem item = expandingList.createNewItem(R.layout.expanding_layout);

        if(item != null) {
            item.setIndicatorColorRes(ColorRes);
            item.setIndicatorIconRes(IconRes);

            TextView tw = (TextView) item.findViewById(R.id.title);
            tw.setText(title);

            item.createSubItems(length);
            for(int i = 0; i<item.getSubItemsCount(); i++) {
                final View view = item.getSubItemView(i);

                configureSubItem(item, view, subItems[i]);
            }

            ImageView addBtn = (ImageView) item.findViewById(R.id.add_more_sub_items);
            addBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    LayoutInflater inflater = (activity).getLayoutInflater();
                    builder.setTitle("Create new entry");
                    builder.setCancelable(true);
                    // set the custom layout
                    final View customLayout = inflater.inflate(R.layout.create_alert_dialog, null);
                    builder.setView(customLayout);
                    // add a button
                    builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final TextView pname = (TextView) customLayout.findViewById(R.id.p_name);
                            TextView pdays = (TextView) customLayout.findViewById(R.id.p_days);

                            if(pname == null || pname.getText().toString().equals("") ||
                                    pdays == null || pdays.getText().toString().equals("") ||
                                    Integer.parseInt(pdays.getText().toString()) == 0) {
                                Toast.makeText(activity, "INVALID DATA!", Toast.LENGTH_SHORT).show();
                            } else {
                                item.createSubItem();
                                final View new_view = item.getSubItemView(item.getSubItemsCount() - 1);
                                final TextView subTitle = new_view.findViewById(R.id.sub_title);
                                final String text = pname.getText().toString() + " - " + pdays.getText().toString() + " DAYS "
                                        + "(" + title + ")";
                                subTitle.setText(removeBracelets(text));
                                Products.add(text);
                                Save();

                                subTitle.setOnClickListener(new View.OnClickListener() {

                                    AlertDialog.Builder copiesDialog = new AlertDialog.Builder(activity);
                                    LayoutInflater inflater = (activity).getLayoutInflater();

                                    @Override
                                    public void onClick(View v) {
                                        copiesDialog.setTitle("How many copies do you want to print ?");
                                        copiesDialog.setCancelable(true);
                                        final View customLayout = inflater.inflate(R.layout.create_alert_copies, null);
                                        copiesDialog.setView(customLayout);
                                        copiesDialog.setPositiveButton("Print", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                TextView copiesText = (TextView) customLayout.findViewById(R.id.l_copies);
                                                int copies = Integer.parseInt(copiesText.getText().toString());

                                                if(copies == 0 || copies > 10) {
                                                    Toast.makeText(activity, "INVALID VALUE!\n 0 < copies <= 10", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    item.toggleExpanded();

                                                    @SuppressLint("SimpleDateFormat")
                                                    DateFormat df = new SimpleDateFormat(DATE_FORMAT_NOW);
                                                    Calendar cal = Calendar.getInstance();
                                                    Calendar cal2 = Calendar.getInstance();
                                                    cal2.add(Calendar.DAY_OF_MONTH, StringToPrintInfoDays(text));
                                                    cal2.set(Calendar.HOUR_OF_DAY, 23);
                                                    cal2.set(Calendar.MINUTE, 59);
                                                    cal2.set(Calendar.SECOND, 59);

                                                    String messageToPrint = "Name: " + StringToPrintInfoName(text) + "\n" +
                                                            "R: " + df.format(cal.getTime())+ "\n" +
                                                            "D: "+ df.format(cal2.getTime()) + "\n";

                                                    for(int i=0; i<copies; i++)
                                                        PrintText(messageToPrint);

                                                    for(int i=0; i<expandingList.getItemsCount(); i++) {
                                                        ExpandingItem ei = expandingList.getItemByIndex(i);
                                                        if(ei.isExpanded())
                                                            ei.toggleExpanded();
                                                    }
                                                }
                                            }
                                        });
                                        copiesDialog.setNegativeButton("Stop", null);
                                        copiesDialog.create().show();
                                    }
                                });

                                new_view.findViewById(R.id.remove_sub_item).setOnClickListener(new View.OnClickListener() {

                                    AlertDialog.Builder final_dialog = new AlertDialog.Builder(activity);

                                    @Override
                                    public void onClick(final View view) {
                                        final_dialog.setTitle("Are you sure ?");
                                        final_dialog.setIcon(android.R.drawable.ic_dialog_alert);
                                        final_dialog.setMessage("Do you really want to remove " + pname.getText().toString() + " ?");
                                        final_dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                item.removeSubItem(new_view);
                                                Products.remove(text);
                                                Save();
                                                Toast.makeText(activity, "ITEM REMOVED !", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        final_dialog.setNegativeButton("No", null);
                                        final_dialog.create().show();
                                    }
                                });
                            }
                        }
                    });
                    builder.setNegativeButton("CANCEL", null);
                    builder.create().show();
                }
            });
        }
    }

    private void configureSubItem(final ExpandingItem item, final View view, final String subTitle) {
        if(!Products.contains(subTitle)) {
            Products.add(subTitle);
        }

        TextView tw = (TextView) view.findViewById(R.id.sub_title);
        tw.setText(removeBracelets(subTitle));

        tw.setOnClickListener(new View.OnClickListener() {

            AlertDialog.Builder copiesDialog = new AlertDialog.Builder(activity);
            LayoutInflater inflater = (activity).getLayoutInflater();

            @Override
            public void onClick(View v) {
                copiesDialog.setTitle("How many copies do you want to print ?");
                copiesDialog.setCancelable(true);
                final View customLayout = inflater.inflate(R.layout.create_alert_copies, null);
                copiesDialog.setView(customLayout);
                copiesDialog.setPositiveButton("Print", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextView copiesText = (TextView) customLayout.findViewById(R.id.l_copies);
                        int copies = Integer.parseInt(copiesText.getText().toString());

                        if(copies == 0 || copies > 10) {
                            Toast.makeText(activity, "INVALID VALUE!\n 0 < copies <= 10", Toast.LENGTH_SHORT).show();
                        } else {
                            item.toggleExpanded();

                            @SuppressLint("SimpleDateFormat")
                            DateFormat df = new SimpleDateFormat(DATE_FORMAT_NOW);
                            Calendar cal = Calendar.getInstance();
                            Calendar cal2 = Calendar.getInstance();
                            cal2.add(Calendar.DAY_OF_MONTH, StringToPrintInfoDays(subTitle));
                            cal2.set(Calendar.HOUR_OF_DAY, 23);
                            cal2.set(Calendar.MINUTE, 59);
                            cal2.set(Calendar.SECOND, 59);

                            String messageToPrint = "Name: " + StringToPrintInfoName(subTitle) + "\n" +
                                    "R: " + df.format(cal.getTime())+ "\n" +
                                    "D: "+ df.format(cal2.getTime()) + "\n";

                            for(int i=0; i<copies; i++)
                                PrintText(messageToPrint);

                            for(int i=0; i<expandingList.getItemsCount(); i++) {
                                ExpandingItem ei = expandingList.getItemByIndex(i);
                                if(ei.isExpanded())
                                    ei.toggleExpanded();
                            }
                        }
                    }
                });
                copiesDialog.setNegativeButton("Stop", null);
                copiesDialog.create().show();
            }
        });

        final AlertDialog.Builder final_dialog = new AlertDialog.Builder(activity);
        final_dialog.setIcon(android.R.drawable.ic_dialog_alert);
        final_dialog.setTitle("Are you sure ?");
        final_dialog.setMessage("Do you really want to remove '" + subTitle + "' ?");
        final_dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(activity, "ITEM REMOVED ! | " + subTitle, Toast.LENGTH_LONG).show();
                Products.remove(subTitle);
                Save();
                item.removeSubItem(view);
            }
        });
        final_dialog.setNegativeButton("No", null);

        view.findViewById(R.id.remove_sub_item).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view2) {
                final_dialog.create().show();
            }
        });
    }

    // ------- Save & Load data

    private void Save() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Listofproducts", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        StringBuilder csvList = new StringBuilder();
        for(String s : Products) {
            csvList.append(s);
            csvList.append(",");
        }

        editor.clear();
        editor.putString("Products", csvList.toString());
        editor.apply();
    }

    private void Load() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Listofproducts", Context.MODE_PRIVATE);

        String csvList = sharedPref.getString("Products", null);
        if(csvList != null) {
            String[] items = csvList.split(",", 0);
            Collections.addAll(Products, items);
        }
    }

    // ------ Utils

    private String removeBracelets(String str) {
        int indexOf = str.indexOf('(');
        return str.substring(0, indexOf);
    }

    private String StringToPrintInfoName(String str) {
        // "name - x days - (category)" => "name"
        int indexOf = str.lastIndexOf('-');
        return str.substring(0, indexOf);
    }

    private int StringToPrintInfoDays(String str) {
        // "name - x days - (category)" => x
        str = str.substring(str.indexOf('-'), str.lastIndexOf('('));
        str = str.replaceAll("\\D+", "");
        return Integer.parseInt(str);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Gata serviciu ? :))")
                .setMessage("Are you sure you want to close this application ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .create().show();
    }
}