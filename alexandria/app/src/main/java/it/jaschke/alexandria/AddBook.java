package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = "AddBook";

    private EditText editText;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT ="eanContent";
    private final String IMAGE_TAG   ="image";
    private final String ISBN_978    = "978";

    private OnAddBookListener listener;
    public interface OnAddBookListener {
        public void onSetTitle(int titleId);
    }

    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(editText !=null) {
            outState.putString(EAN_CONTENT, editText.getText().toString());
        }

        if (rootView != null) {
            ImageView imageView = (ImageView) rootView.findViewById(R.id.bookCover);
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            if (drawable != null) {
                Bitmap bitmap = drawable.getBitmap();
                outState.putParcelable(IMAGE_TAG, bitmap);
            }
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        editText = (EditText) rootView.findViewById(R.id.ean);

        // prevent copy, paste
        editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String enteredText = s.toString();
                //catch isbn10 numbers
                if (enteredText.length() == 10 && !enteredText.startsWith(ISBN_978)) {
                    enteredText = ISBN_978 + enteredText;
                }

                if (enteredText.length() < 13) {
                    Log.d(LOG_TAG, "afterTextChanged enteredText = " + enteredText);

                    return;
                }

                clearFields();

                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, enteredText);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            editText.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, editText.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                editText.setText("");
                clearFields();
            }
        });

        if(savedInstanceState!=null){
            editText.setText(savedInstanceState.getString(EAN_CONTENT));
            editText.setHint("");

            Bitmap bitmap = savedInstanceState.getParcelable(IMAGE_TAG);
            if (bitmap != null) {
                ImageView imageView = (ImageView) rootView.findViewById(R.id.bookCover);
                imageView.setImageBitmap(bitmap);
            }
        }

        return rootView;
    }

    private void restartLoader(){
        Log.d(LOG_TAG, "restartLoader");
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");

        if(editText.getText().length()==0){
            return null;
        }
        String eanStr= editText.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith(ISBN_978)){
            eanStr = ISBN_978 + eanStr;
        }

        HideKeyboard();

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        if (bookTitle != null) {    //kmm adding null check
            ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);
            Log.d(LOG_TAG, "onLoadFinished bookTitle = " +bookTitle);
        }

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        if (bookSubTitle != null) {    //kmm adding null check, not all books have subtitles
            ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);
        }

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {    //kmm adding null check, some books may not list author
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if((imgUrl != null) && Patterns.WEB_URL.matcher(imgUrl).matches()){    //kmm adding null check
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
    }

    private void clearFields(){
        Log.d(LOG_TAG, "clearFields");

        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        ((ImageView) rootView.findViewById(R.id.bookCover)).setImageBitmap(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(LOG_TAG, "onAttach");

        if (activity instanceof OnAddBookListener) {
            listener = (OnAddBookListener) activity;
            listener.onSetTitle(R.string.add_book);

        } else {
            throw new ClassCastException(activity.toString()
                    + " must implemenet ListOfBooks.OnListOfBooksListener");
        }
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");

        if (listener != null) {
            listener.onSetTitle(R.string.add_book);
        }

        super.onResume();
    }

    private void HideKeyboard() {
        Log.d(LOG_TAG, "HideKeyboard");
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(editText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(LOG_TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }
}
