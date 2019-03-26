package it.uniroma1.boubouk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.boubouk.booksapi.ApiInterface;
import it.uniroma1.boubouk.booksapi.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String TAG = MainActivity.class.getSimpleName();

    private FirebaseUser currentUser;
    private GridView gridBooks;
    private ImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private View searchBookView;
    private Uri photoUrl;

    private List<Book> books = new ArrayList<>();

    // TEMPORARY
    private Book book;

    public View getViewForSearchBook(Context context) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.search_book, null);

        final RadioButton rb1 = view.findViewById(R.id.keyword_radiobutton);
        final RadioButton rb2 = view.findViewById(R.id.isbn_radiobutton);
        final EditText et1 = view.findViewById(R.id.keyword_edit_text);
        final EditText et2 = view.findViewById(R.id.isbn_edit_text);
        final ImageView photo_isbn = view.findViewById(R.id.photo_isbn);


        View.OnClickListener listener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //rb2.setEnabled(false);
                et2.setEnabled(false);
                photo_isbn.setEnabled(false);
                //rb1.setEnabled(true);
                et1.setEnabled(true);
            }
        };

        View.OnClickListener listener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //rb1.setEnabled(false);
                et1.setEnabled(false);
                //rb2.setEnabled(true);
                et2.setEnabled(true);
                photo_isbn.setEnabled(true);
            }
        };

        rb1.setOnClickListener(listener1);
        et1.setOnClickListener(listener1);
        rb2.setOnClickListener(listener2);
        et2.setOnClickListener(listener2);

        /*// Setup the Barcode Detector
        BarcodeDetector detector =
                new BarcodeDetector.Builder(context)
                        .setBarcodeFormats(Barcode.DATA_MATRIX)
                        .build();
        if (!detector.isOperational()) {
            Log.e("Searchbook", "Detector not operational");
        }
        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);
        Barcode thisCode = barcodes.valueAt(0);
        TextView txtView = (TextView) findViewById(R.id.txtContent);
        txtView.setText(thisCode.rawValue);*/


        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Add account info in the navigation panel
        View headerView = navigationView.getHeaderView(0);
        profileImage = headerView.findViewById(R.id.profileImage);
        profileName = headerView.findViewById(R.id.profileName);
        profileEmail = headerView.findViewById(R.id.profileEmail);
        if (currentUser != null) {
            photoUrl = currentUser.getPhotoUrl();
        }
        profileImage.setImageURI(photoUrl);
        profileName.setText(currentUser.getDisplayName());
        profileEmail.setText(currentUser.getEmail());

        searchBookView = getViewForSearchBook(this);
        // Array di prova
        books.add(new Book("harry potter e daniele sinibaldi",
                "cecilia podagrosi",
                "lorenzo di marzio",
                "69",
                69,
                1994,
                "",
                "un bel giorno arriva cecilia al diag e succederà qualcosa"));
        books.add(new Book("harry potter e daniele sinibaldi2",
                "cecilia podagrosi",
                "lorenzo di marzio",
                "69",
                69,
                1994,
                "",
                "un bel giorno arriva cecilia al diag e succederà qualcosa"));
        // Set variables from layout
        gridBooks = findViewById(R.id.grid_books);
        gridBooks.setAdapter(new DragGridAdapter(this, books));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        if (id == R.id.action_add) {
            AlertDialog alertDialog = Util.createDoubleButtonAlertDialogWithCustomView(
                    this,
                    getString(R.string.add_book),
                    searchBookView,
                    getString(R.string.search),
                    getString(R.string.cancel),
                    false
            );
            alertDialog.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.search),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText keywordEditText = searchBookView.findViewById(R.id.keyword_edit_text);
                            EditText isbnEditText = searchBookView.findViewById(R.id.isbn_edit_text);

                            if (keywordEditText.isEnabled()) {
                                String keyword = keywordEditText.getText().toString();

                            } else if (isbnEditText.isEnabled()) {
                                String isbn = isbnEditText.getText().toString();
                            }
                            getBooks("");

                        }
                    });
            alertDialog.show();
            return true;
        } else if (id == R.id.action_sort) {
            //todo aggiungere sort
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_books) {
            // Handle the camera action
        } else if (id == R.id.nav_libraries) {

        } else if (id == R.id.nav_signout) {
            FirebaseAuth.getInstance().signOut();
            Intent signOutIntent = new Intent(this, LoginActivity.class);

            startActivity(signOutIntent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    private void getBooks(String keyword) {
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        Call<List<Book>> call = apiInterface.getBooks("https://www.googleapis.com/books/v1/volumes?q=qualcosa");
        call.enqueue(new Callback<List<Book>>() {

            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful()) {
                    for (Book b : response.body()) {
                        books.add(b);
                    }
                } else {
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }
}
