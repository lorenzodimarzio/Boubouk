package it.uniroma1.boubouk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.uniroma1.boubouk.classes.Book;

public class SearchOnlineActivity extends AppCompatActivity {

    public static final String FIXED_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    // Constants string for parsing Google Books API response
    public static final String ERROR = "error";
    public static final String TOTAL_ITEMS = "totalItems";
    public static final String ITEMS = "items";
    public static final String VOLUME_INFO = "volumeInfo";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String AUTHORS = "authors";
    public static final String PUBLISHER = "publisher";
    public static final String INDUSTRY_IDENTIFIERS = "industryIdentifiers";
    public static final String IDENTIFIER = "identifier";
    public static final String TYPE = "type";
    public static final String ISBN_13 = "ISBN_13";
    public static final String PAGE_COUNT = "pageCount";
    public static final String PUBLISHED_DATE = "publishedDate";
    public static final String IMAGE_LINKS = "imageLinks";
    public static final String THUMBNAIL = "thumbnail";
    public static final String DESCRIPTION = "description";
    public static final String SALE_INFO = "saleInfo";
    public static final String BUY_LINK = "buyLink";
    private static final int REQUEST_CODE = 1234;
    private ArrayList<Book> addedBooks = new ArrayList<>();
    private boolean voiceEnabled = true;
    private EditText searchEditText;
    private ImageView crossVoice;
    private ProgressBar progressBar;
    private RecyclerView searchResultView;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_online);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchEditText = findViewById(R.id.search_edittext);
        crossVoice = findViewById(R.id.crossVoice);
        progressBar = findViewById(R.id.progress_bar);
        queue = Volley.newRequestQueue(this);
        searchResultView = findViewById(R.id.search_results_view);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    crossVoice.setImageDrawable(getResources().getDrawable(
                            R.drawable.ic_keyboard_voice_black_24dp)
                    );
                    voiceEnabled = true;
                } else {
                    crossVoice.setImageDrawable(getResources().getDrawable(
                            R.drawable.ic_clear_black_24dp)
                    );
                    voiceEnabled = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = searchEditText.getText().toString();
                    if (text.matches("[0-9]{13}")) {
                        sendSearchRequest(text, false);
                    } else {
                        sendSearchRequest(text, true);
                    }
                    InputMethodManager inputManager =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager != null && getCurrentFocus() != null) {
                        inputManager.hideSoftInputFromWindow(
                                getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);

                    }
                    displayProgressBar(true);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition engine thought it heard
            final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (!matches.isEmpty()) {
                String text = matches.get(0);
                searchEditText.setText(text);
                sendSearchRequest(text, true);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.BOOK_EXTRA, addedBooks);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void inflateSearchView(ArrayList<Book> books) {
        searchResultView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        searchResultView.setLayoutManager(new LinearLayoutManager(this));

        SearchResultsAdapter searchResultsAdapter = new SearchResultsAdapter(this, books);
        searchResultView.setAdapter(searchResultsAdapter);
    }

    public ArrayList<Book> parseSearchResponse(JSONObject response) {
        // Assuming that totalItems equals _books.size()
        int totalItems;
        ArrayList<Book> res = new ArrayList<>();
        if (response.has(ERROR)) {
            return null;
        } else {
            try {
                JSONArray _books;
                if (!response.has(TOTAL_ITEMS) || !response.has(ITEMS)) {
                    return null;
                }
                totalItems = response.getInt(TOTAL_ITEMS);
                if (totalItems > 10) {
                    totalItems = 10;
                }
                _books = response.getJSONArray(ITEMS);

                for (int i = 0; i < totalItems; i++) {
                    JSONObject _book = _books.getJSONObject(i);
                    if (!_book.has(ID)) {
                        continue;
                    }
                    if (!_book.has(VOLUME_INFO)) {
                        continue;
                    }
                    JSONObject _info = _book.getJSONObject(VOLUME_INFO);
                    if (!_info.has(TITLE) || !_info.has(AUTHORS)) {
                        continue;
                    }
                    String _id = _book.getString(ID);
                    String _title = _info.getString(TITLE);
                    JSONArray _authors = _info.getJSONArray(AUTHORS);
                    String[] _authorsArray = new String[_authors.length()];
                    for (int j = 0; j < _authors.length(); j++) {
                        _authorsArray[j] = _authors.getString(j);
                    }
                    String _authorsString = TextUtils.join(", ", _authorsArray);
                    String _publisher = "";
                    String _isbn = "";
                    if (_info.has(INDUSTRY_IDENTIFIERS)) {
                        JSONArray _ids = _info.getJSONArray(INDUSTRY_IDENTIFIERS);
                        for (int j = 0; j < _ids.length(); j++) {
                            JSONObject oid = _ids.getJSONObject(j);
                            if (!oid.has(TYPE)) {
                                continue;
                            }
                            if (oid.getString(TYPE).equals(ISBN_13) && oid.has(IDENTIFIER)) {
                                _isbn = oid.getString(IDENTIFIER);
                                break;
                            }
                        }
                    }
                    int _pages = -1;
                    int _year = -1;
                    String _image = "";
                    String _plot = "";
                    String _buyLink = null;
                    if (_info.has(PUBLISHER)) {
                        _publisher = _info.getString(PUBLISHER);
                    }
                    if (_info.has(PAGE_COUNT)) {
                        _pages = _info.getInt(PAGE_COUNT);
                    }
                    if (_info.has(PUBLISHED_DATE)) {
                        _year = Integer.parseInt(
                                _info.getString(PUBLISHED_DATE).substring(0, 4));
                    }
                    if (_info.has(IMAGE_LINKS)) {
                        JSONObject _imageLinks = _info.getJSONObject(IMAGE_LINKS);
                        if (_imageLinks.has(THUMBNAIL)) {
                            _image = _imageLinks.getString(THUMBNAIL);
                        }
                    }
                    if (_info.has(DESCRIPTION)) {
                        _plot = _info.getString(DESCRIPTION);
                    }
                    if (_book.has(SALE_INFO)) {
                        JSONObject _sale_info = _book.getJSONObject(SALE_INFO);
                        if (_sale_info.has(BUY_LINK)) {
                            _buyLink = _sale_info.getString(BUY_LINK);
                        }
                    }
                    res.add(new Book(_id, _title, _authorsString, _publisher, _isbn,
                            _pages, _year, _image,  _plot, _buyLink));
                }
            } catch (JSONException ex) {
                Util.createSingleButtonAlertDialog(
                        this,
                        "",
                        ex.getMessage(),
                        getString(R.string.ok),
                        true
                ).show();
                return null;
            }
            return res;
        }
    }


    public void sendSearchRequest(final String text, final boolean searchByKeyword) {
        if (Util.checkConnection(this)) {
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    FIXED_URL + (searchByKeyword ? text : "isbn:" + text),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ArrayList<Book> resultBooks = parseSearchResponse(response);
                            if (resultBooks != null) {
                                if (resultBooks.size() != 0) {
                                    inflateSearchView(resultBooks);
                                } else {
                                    Util.createSingleButtonAlertDialog(
                                            SearchOnlineActivity.this,
                                            getString(R.string.warning),
                                            getString(R.string.no_books_found),
                                            getString(R.string.ok),
                                            true
                                    ).show();
                                }
                            } else {
                                Util.createSingleButtonAlertDialog(
                                        SearchOnlineActivity.this,
                                        getString(R.string.warning),
                                        getString(R.string.error_in_the_request),
                                        getString(R.string.ok),
                                        true
                                ).show();
                            }
                            displayProgressBar(false);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String msg = error.getMessage();
                            Util.createSingleButtonAlertDialog(
                                    SearchOnlineActivity.this,
                                    getString(R.string.warning),
                                    getString(R.string.error_in_the_response) + "\nError message:\n" + msg,
                                    getString(R.string.ok),
                                    true
                            ).show();
                            displayProgressBar(false);
                        }
                    }
            );
            queue.add(request);
        } else {
            displayProgressBar(false);
            Util.notifyNoConnection(this);
        }
    }

    public void addBookToAdded(Book book) {
        addedBooks.add(book);
    }

    public void clearSearchText(View view) {
        if (voiceEnabled) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.search_by_keyword_title_author_isbn));
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            searchEditText.setText("");
        }
    }

    public void onActionAddBook(Book book) {
        addBookToAdded(book);
        Toast toast = Toast.makeText(
                this,
                getString(R.string.book_added),
                Toast.LENGTH_SHORT
        );
        toast.show();
    }

    public void displayProgressBar(boolean display) {
        if (display) {
            progressBar.setVisibility(View.VISIBLE);
            searchResultView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            searchResultView.setVisibility(View.VISIBLE);
        }
    }
}