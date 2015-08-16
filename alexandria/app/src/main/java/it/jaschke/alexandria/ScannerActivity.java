package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by kenm on 8/1/2015.
 */
public class ScannerActivity  extends ActionBarActivity
                              implements ZBarScannerView.ResultHandler,
                                         LoaderManager.LoaderCallbacks<Cursor>,
                                         ADialogFragment.YesNoListener{
    private ZBarScannerView mScannerView;
    private static final String LOG_TAG = "ScannerActivity";
    private final int LOADER_ID = 1;

    private String mContents;
    private String mFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner);
        getSupportActionBar().hide();

        mScannerView = new ZBarScannerView(this);

        ArrayList<BarcodeFormat> formats = new ArrayList<>(1);
        formats.add(BarcodeFormat.ISBN13);
//        formats.add(BarcodeFormat.ISBN10);
        formats.add(BarcodeFormat.EAN13);
        mScannerView.setFormats(formats);

        setContentView(mScannerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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


    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");

        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");

        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        mContents = rawResult.getContents();
        if (mContents.length() == 10 && !mContents.startsWith("978")) {
            mContents = "978" + mContents;
        }

        mFormat = rawResult.getBarcodeFormat().getName();

        Log.d(LOG_TAG, "handleResult contents = " + mContents);
        Log.d(LOG_TAG, "handleResult format = " + mFormat);


        String toastText = "Contents = " + mContents + ", Format = " + mFormat;
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

        addBook(mContents);
    }

    private void addBook(String isbnNumber) {
        Log.d(LOG_TAG, "addBook isbnNumber = " +isbnNumber);

        Intent bookIntent = new Intent(this, BookService.class);
        bookIntent.putExtra(BookService.EAN, isbnNumber);
        bookIntent.setAction(BookService.FETCH_BOOK);
        startService(bookIntent);

        restartLoader();
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");

        if (mContents.length()==10 && !mContents.startsWith("978")) {
            mContents = "978" + mContents;
        }

        return new CursorLoader(
                this,
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(mContents)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");

        if (!data.moveToFirst()) {
            Log.d(LOG_TAG, "onLoadFinished No Data");
            mScannerView.startCamera();
            return;
        }

        mScannerView.stopCamera();

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        if (bookTitle != null) {
            Log.d(LOG_TAG, "onLoadFinished bookTitle = " +bookTitle);
        }

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        if (bookSubTitle != null) {
            Log.d(LOG_TAG, "onLoadFinished bookSubTitle = " +bookSubTitle);
        }

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            Log.d(LOG_TAG, "onLoadFinished authors = " +authors);
        }

        displayDialog(bookTitle);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
    }

    private void restartLoader(){
        Log.d(LOG_TAG, "restartLoader");
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void deleteBook() {
        Intent bookIntent = new Intent(this, BookService.class);
        bookIntent.putExtra(BookService.EAN, mContents);
        bookIntent.setAction(BookService.DELETE_BOOK);
        startService(bookIntent);
    }

    private void displayDialog(String bookTitle) {
        String msgTitle = bookTitle + getString(R.string.booktitle_found);
        String txtMessage = getString(R.string.save_this_book);

        ADialogFragment dlg = new ADialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ADialogFragment.DIALOG_TITLE, msgTitle);
        bundle.putString(ADialogFragment.DIALOG_MESSAGE, txtMessage);
        dlg.setArguments(bundle);
        dlg.show(getFragmentManager(), ADialogFragment.A_DIALOG_FRAGMENT);
    }

    public void onYes(){
        Log.d(LOG_TAG, "onYes");

        mScannerView.startCamera();
    }

    public void onNo(){
        Log.d(LOG_TAG, "onNo");

        deleteBook();
        mScannerView.startCamera();
    }
}
