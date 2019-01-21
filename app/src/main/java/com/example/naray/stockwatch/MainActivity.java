package com.example.naray.stockwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.text.method.ScrollingMovementMethod;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnLongClickListener,firstAsyncResponse,secondAsyncResponse {



    private List<Stock> stockList = new ArrayList<>();

    private RecyclerView recyclerview; // Layout's recyclerview
    private SwipeRefreshLayout swiper;

    private StockAdapter sAdapter;
    private DatabaseHandler databasehandler;
    private Context context;
    private MainActivity ma;
    private static String stockwatchURL = "http://www.marketwatch.com/investing/stock/";
    //AsyncLoaderTask asyncTask = new AsyncLoaderTask();
    //AsyncFLoaderTask asyncFTask = new AsyncFLoaderTask();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerview = (RecyclerView)findViewById(R.id.recycler);
        sAdapter = new StockAdapter(stockList, this);
        recyclerview.setAdapter(sAdapter);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        databasehandler = new DatabaseHandler(this);
        if(networkCheck()) {
            ArrayList<Stock> list = databasehandler.loadStocks();
            //AsyncLoaderTask asyncTask = new AsyncLoaderTask(MainActivity.this);
            //AsyncFLoaderTask asyncFtask = new AsyncFLoaderTask(MainActivity.this);
            //asyncTask.res = this;
            //asyncFtask.res2=this;
            int m = list.size();
            if (m == 0) {
                // do nothing
            } else {
                for (int i = 0; i < m; i++) {
              /*AsyncLoaderTask asyncLoaderTask = new AsyncLoaderTask();
              asyncLoaderTask.res = this;
              asyncLoaderTask.execute(list.get(i).getStock_symbol());*/
             /*   AsyncLoaderTask asyncTask = new AsyncLoaderTask(MainActivity.this);
                asyncTask.execute(list.get(i).getStock_symbol());*/
                    AsyncFLoaderTask asyncFTask = new AsyncFLoaderTask(MainActivity.this);
                    asyncFTask.execute(list.get(i).getStock_symbol(), list.get(i).getCompany());
                }
            }
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please turnon your internet");
            builder.setTitle("No Network Connection");

            AlertDialog dialog = builder.create();
            dialog.show();

        }
        swiper = (SwipeRefreshLayout)findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });
    }

    private void doRefresh() {
        if (networkCheck()) {
            List<Stock> copyStock = databasehandler.loadStocks();
            stockList.clear();
            for (int i = 0; i < copyStock.size(); i++) {

                AsyncFLoaderTask asyncFTask = new AsyncFLoaderTask(MainActivity.this);
                asyncFTask.execute(copyStock.get(i).getStock_symbol(), copyStock.get(i).getCompany());
            }

            swiper.setRefreshing(false);
            sAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Stocks Updated", Toast.LENGTH_SHORT).show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stocks cannot be updated without internet connection");
            builder.setTitle("No Network Connection");

            AlertDialog dialog = builder.create();
            dialog.show();
            swiper.setRefreshing(false);

        }
    }

    public boolean networkCheck(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo  netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @Override
    public void onClick(View v) {  // click listener called by ViewHolder clicks

        final int pos = recyclerview.getChildLayoutPosition(v);
        Stock s = stockList.get(pos);
        String url = stockwatchURL+s.getStock_symbol();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);


        Toast.makeText(v.getContext(),  s.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(View v) {  // long click listener called by ViewHolder long clicks
        final int pos = recyclerview.getChildLayoutPosition(v);
        Stock s = stockList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                databasehandler.deleteStock(stockList.get(pos).getStock_symbol());
                stockList.remove(pos);
                sAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage("Delete StockSymbol " + stockList.get(pos).getStock_symbol() + "?");
        builder.setTitle("Delete Stock");

        AlertDialog dialog = builder.create();
        dialog.show();


        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add:
                //addStock(v);
                if (networkCheck()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    final EditText et = new EditText(this);
                    et.setInputType(InputType.TYPE_CLASS_TEXT);
                    et.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                    et.setGravity(Gravity.CENTER_HORIZONTAL);

                    builder.setView(et);
                    builder.setMessage("Please enter a stock symbol:");
                    builder.setTitle("Stock Selection");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String userEnteredStockSymbol = et.getText().toString().trim();
                            /*AsyncLoaderTask asyncLoaderTask = new AsyncLoaderTask();
                            asyncLoaderTask.res = ma;
                            asyncLoaderTask.execute(userEnteredStockSymbol);*/
                            AsyncLoaderTask asyncTask = new AsyncLoaderTask(MainActivity.this);
                            asyncTask.execute(userEnteredStockSymbol);

                        }
                    });
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Stocks cannot be added without internet connection");
                    builder.setTitle("No Network Connection");

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                default:
                    return super.onOptionsItemSelected(item);

        }


    }

    public void processNewStock(final ArrayList<Stock> myasyncList,String userText){

        final List<String> lsArray = new ArrayList<String>();
        for (int i = 0; i < myasyncList.size(); i++) {
            if(myasyncList.get(i).getStock_symbol().startsWith(userText)) {
                Log.i(userText,"***********************************************************This is usertext");
                lsArray.add(myasyncList.get(i).getStock_symbol() + " - " + myasyncList.get(i).getCompany());
            }
        }

        if(lsArray.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Data for stock symbol");
            builder.setTitle("Symbol Not Found:"+userText);

            AlertDialog dialog = builder.create();
            dialog.show();

        }

        else if(lsArray.size()==1){
            int index = lsArray.get(0).indexOf("-");
            String stocksymbol = lsArray.get(0).substring(0,index-1);
            String stockcompany = lsArray.get(0).substring(index+2,lsArray.get(0).length());

            /*AsyncFLoaderTask asyncFLoaderTask = new AsyncFLoaderTask();
            asyncFLoaderTask.res2=this;
            asyncFLoaderTask.execute(stocksymbol,stockcompany);*/
            AsyncFLoaderTask asyncFTask = new AsyncFLoaderTask(MainActivity.this);
            asyncFTask.execute(stocksymbol,stockcompany);
        }

        else{
            //final CharSequence[] lsArray = new CharSequence[myasyncList.size()];

            CharSequence[] itemsArray = lsArray.toArray(new CharSequence[lsArray.size()]);

            Log.d(String.valueOf(lsArray),"This is list array");

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Make a selection");
            builder.setItems(itemsArray, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                   int index = lsArray.get(which).indexOf("-");
                   String stocksymbol = lsArray.get(which).substring(0,index-1);
                    String stockcompany = lsArray.get(which).substring(index+2,lsArray.get(which).length());
                   //String stocksymbol =  lsArray.get(which).getStock_symbol();
                   //String stockcompany = lsArray.get(which).getCompany();

                   /*AsyncFLoaderTask asyncFLoaderTask = new AsyncFLoaderTask();
                   asyncFLoaderTask.res2=ma;
                   asyncFLoaderTask.execute(stocksymbol,stockcompany);*/
                    AsyncFLoaderTask asyncFTask = new AsyncFLoaderTask(MainActivity.this);
                    asyncFTask.execute(stocksymbol,stockcompany);
                }
            });

            builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    public void addNewStock(Stock stockdata){
        if(stockList.size()!=0) {
            if (stockList.toString().contains(stockdata.getCompany())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Stock Symbol " + stockdata.getStock_symbol() + " is already displayed");
                builder.setTitle("Duplicate Stock");
                builder.setIcon(R.drawable.warning);

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                stockList.add(stockdata);
                Collections.sort(stockList);
                databasehandler.addStock(stockdata);
                sAdapter.notifyDataSetChanged();
            }
        }
        else{
            stockList.add(stockdata);
            Collections.sort(stockList);
            Log.d(String.valueOf(stockdata),"This is stockdata in mainactivity");
            databasehandler.addStock(stockdata);
            sAdapter.notifyDataSetChanged();
        }
    }
}


