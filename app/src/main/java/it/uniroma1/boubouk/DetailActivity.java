package it.uniroma1.boubouk;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import it.uniroma1.boubouk.classes.Book;
import it.uniroma1.boubouk.db.AppDatabase;

public class DetailActivity extends AppCompatActivity {
    private AppDatabase db;
    private Book book;
    private TextView title;
    private TextView author;
    private TextView date;
    private TextView publisher;
    private TextView pages;
    private TextView isbn;
    private TextView plot;
    private ImageView cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up member variables
        db = AppDatabase.getAppDatabase(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            book = (Book) extras.get(MainActivity.BOOK_EXTRA);
        }

        title = findViewById(R.id.details_title);
        author = findViewById(R.id.details_author);
        date = findViewById(R.id.details_date);
        publisher = findViewById(R.id.details_publisher);
        pages = findViewById(R.id.details_pages);
        isbn = findViewById(R.id.details_isbn);
        plot = findViewById(R.id.details_plot);
        cover = findViewById(R.id.details_cover);

        // Set texts
        String dateString = book.getYear() == -1 ? "" : String.valueOf(book.getYear());
        String pagesString = book.getPages() == -1 ? "" : String.valueOf(book.getPages());

        SpannableString _author = new SpannableString("Author: " + book.getAuthor());
        SpannableString _date = new SpannableString("Published date: " + dateString);
        SpannableString _publisher = new SpannableString("Publisher: " + book.getPublisher());
        SpannableString _pages = new SpannableString("Pages: " + pagesString);
        SpannableString _isbn = new SpannableString("ISBN: " + book.getIsbn());

        _author.setSpan(Typeface.BOLD, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        _date.setSpan(Typeface.BOLD, 0, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        _publisher.setSpan(Typeface.BOLD, 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        _pages.setSpan(Typeface.BOLD, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        _isbn.setSpan(Typeface.BOLD, 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        title.setText(book.getTitle());
        author.setText(_author, TextView.BufferType.SPANNABLE);
        date.setText(_date, TextView.BufferType.SPANNABLE);
        publisher.setText(_publisher, TextView.BufferType.SPANNABLE);
        pages.setText(String.valueOf(_pages), TextView.BufferType.SPANNABLE);
        isbn.setText(_isbn, TextView.BufferType.SPANNABLE);
        plot.setText(book.getPlot());

        // Set cover image
        cover.setImageBitmap(Util.loadImage(this, book.getId()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_buy) {
            String link = book.getBuyLink();
            if (link == null) {
                Util.createSingleButtonAlertDialog(
                        this,
                        "",
                        getString(R.string.ebook_not_available),
                        getString(R.string.ok),
                        true
                ).show();
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getBuyLink()));
                startActivity(browserIntent);
            }
            return true;
        }/* else if (id == R.id.action_add) {

            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);

        return true;
    }
}
