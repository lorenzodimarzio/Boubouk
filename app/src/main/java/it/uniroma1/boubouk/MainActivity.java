package it.uniroma1.boubouk;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;

import it.uniroma1.boubouk.classes.Book;
import it.uniroma1.boubouk.classes.BookOwnership;
import it.uniroma1.boubouk.classes.User;
import it.uniroma1.boubouk.db.AppDatabase;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_CODE = 1;
    public static final String BOOK_EXTRA = "bookExtra";

    private static AppDatabase db;

    private ActionBarDrawerToggle toggle;
    private ArrayList<Book> allBooks;
    private BooksAdapter booksAdapter;
    private boolean sortedByAuthor = true;
    private EditText searchEditText;
    private FirebaseUser currentUser;
    private FloatingActionButton fab;
    private ImageView profileImage;
    private ImageView searchCross;
    private int selectCount = 0;
    private LinearLayout searchBarLayoutLinear;
    private Menu menu;
    private RecyclerView booksView;
    private RelativeLayout searchBarLayout;
    private TextView selectCountTextView;
    //private TextView offline;
    private TextView profileName;
    private TextView profileEmail;
    private String photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorBlack));
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        // Init database
        db = AppDatabase.getAppDatabase(this);

        // Get current user and insert in db if it is not present
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db.setUser(new User(currentUser.getEmail()));
        db.daoAccess().insertUser(new User(currentUser.getEmail()));

        // Init other member variables
        allBooks = new ArrayList<>(Arrays.asList(db.daoAccess().retrieveAllBooks(currentUser.getEmail())));
        searchEditText = findViewById(R.id.search_edittext);
        searchCross = findViewById(R.id.cross);
        booksView = findViewById(R.id.grid_books);
        booksAdapter = new BooksAdapter(this, allBooks, booksView);
        booksAdapter.setHasStableIds(true);
        booksAdapter.sort(sortedByAuthor, true);
        searchBarLayout = findViewById(R.id.search_bar_layout);
        searchBarLayoutLinear = findViewById(R.id.search_bar_layout_linear);
        selectCountTextView = findViewById(R.id.select_count);
        fab = findViewById(R.id.fab);

        // Add account info in the navigation panel
        View headerView = navigationView.getHeaderView(0);
        profileImage = headerView.findViewById(R.id.profileImage);
        profileName = headerView.findViewById(R.id.profileName);
        profileEmail = headerView.findViewById(R.id.profileEmail);
        if (currentUser != null) {
            photoUrl = currentUser.getPhotoUrl().toString();
        }
        //offline = headerView.findViewById(R.id.offline);
        profileName.setText(currentUser.getDisplayName());
        profileEmail.setText(currentUser.getEmail());

        // Add listeners to the search EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                if (count == 0) {
                    searchCross.setVisibility(View.INVISIBLE);
                } else {
                    searchCross.setVisibility(View.VISIBLE);
                }
                booksAdapter.getFilter().filter(text);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set up the books RecyclerView
        booksView.setAdapter(booksAdapter);
        booksView.setLayoutManager(new GridLayoutManager(this, 2));

        new Util.DownloadImageTask(this, profileImage).execute(photoUrl, currentUser.getEmail());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float height = displayMetrics.heightPixels;
        float width = displayMetrics.widthPixels;
        int density = displayMetrics.densityDpi;
    }

    @Override
    protected void onStart() {
        super.onStart();
        clearSearchText(searchEditText);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (selectCount > 0) {
            onActionDeselectAll();
        } else {
            AlertDialog dialog = Util.createDoubleButtonAlertDialog(
                    this,
                    getString(R.string.warning),
                    getString(R.string.want_to_exit),
                    getString(R.string.cancel),
                    getString(R.string.close),
                    true
            );
            dialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    getString(R.string.close),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.super.onBackPressed();
                        }
                    });
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            onActionSort();
            return true;
        } else if (id == R.id.action_delete) {
            onActionDelete();
            return true;
        } else if (id == R.id.select_all) {
            onActionSelectAll();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_library) {

            return true;
        } else if (id == R.id.nav_signout) {
            FirebaseAuth.getInstance().signOut();
            db.setUser(null);
            Intent signOutIntent = new Intent(this, LoginActivity.class);
            startActivity(signOutIntent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        ArrayList<Book> books = (ArrayList<Book>) extras.get(MainActivity.BOOK_EXTRA);
                        addBooks(books);
                    }
                }
            }
        }
    }

    public boolean isSortedByAuthor() {
        return sortedByAuthor;
    }

    public void addBooks(ArrayList<Book> books) {
        // Add book to all books
        //allBooks.addAll(books);
        booksAdapter.add(books);

        booksAdapter.sort(sortedByAuthor, true);

        // Insert book into the db
        for (Book book : books) {
            db.daoAccess().insertBook(book);
            db.daoAccess().insertOwnership(new BookOwnership(currentUser.getEmail(), book.getId()));
        }
    }

    public void removeBooks(ArrayList<Book> books) {
        allBooks.removeAll(books);
        for (Book book : books) {
            booksAdapter.remove(book);
        }

        for (int i = 0; i < books.size(); i++) {
            db.daoAccess().removeOwnership(new BookOwnership(currentUser.getEmail(), books.get(i).getId()));
            if (db.daoAccess().countOwnerships(books.get(i).getId()) == 0) {
                db.daoAccess().removeBook(books.get(i));
            }
        }

        Toast.makeText(this, R.string.books_removed, Toast.LENGTH_SHORT)
                .show();
    }

    public void clearSearchText(View view) {
        searchEditText.setText("");
    }

    public View getViewForSort(final AlertDialog dialog) {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.dialog_sort, null);

        LinearLayout llAuthor = view.findViewById(R.id.sort_by_author);
        LinearLayout llTitle = view.findViewById(R.id.sort_by_title);

        if (sortedByAuthor) {
            llAuthor.getChildAt(1).setVisibility(View.VISIBLE);
            llTitle.getChildAt(1).setVisibility(View.INVISIBLE);
        } else {
            llAuthor.getChildAt(1).setVisibility(View.INVISIBLE);
            llTitle.getChildAt(1).setVisibility(View.VISIBLE);
        }

        llAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortedByAuthor = true;
                booksAdapter.sort(true, true);
                dialog.cancel();
            }
        });

        llTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortedByAuthor = false;
                booksAdapter.sort(false, true);
                dialog.cancel();
            }
        });

        return view;
    }

    public void onActionAdd(View v) {
        if (Util.checkConnection(this)) {
            Intent intent = new Intent(this, SearchOnlineActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Util.notifyNoConnection(this);
        }
    }

    public void onActionDelete() {
        ArrayList<Book> selectedBooks = new ArrayList<>();
        for (Book book : booksAdapter.getFilteredItems()) {
            if (booksAdapter.getSelected(book.getId())) {
                booksAdapter.deselect(book.getId());
                selectedBooks.add(book);
            }
        }
        removeBooks(selectedBooks);
        disableSelectMode();
    }

    public void onActionDeselectAll() {
        for (Book book : booksAdapter.getFilteredItems()) {
            booksAdapter.deselect(book.getId());
        }
    }

    public void onActionSelectAll() {
        for (Book book : booksAdapter.getFilteredItems()) {
            booksAdapter.select(book.getId());
        }
    }

    public void onActionSort() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        final View sortView = getViewForSort(dialog);
        dialog.setView(sortView);
        dialog.setCancelable(true);
        dialog.show();
    }

    public int getSelectCount() {
        return selectCount;
    }

    public void addSelectCount() {
        this.selectCount++;
    }

    public void removeSelectCount() {
        if (selectCount > 0) {
            this.selectCount--;
        }
    }

    @SuppressLint("RestrictedApi")
    public void setupSelectMode() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.colorSelected)
            ));
            getSupportActionBar().setTitle(null);
        }

        searchBarLayout.setBackgroundColor(getResources().getColor(R.color.colorSelected));
        searchBarLayoutLinear.setVisibility(View.INVISIBLE);

        menu.findItem(R.id.action_delete).setVisible(true);
        menu.findItem(R.id.action_sort).setVisible(false);

        selectCountTextView.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);

        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(null);
    }

    @SuppressLint("RestrictedApi")
    public void disableSelectMode() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.colorPrimary)
            ));
            getSupportActionBar().setTitle(R.string.app_name);
        }

        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        searchBarLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        searchBarLayoutLinear.setVisibility(View.VISIBLE);

        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_sort).setVisible(true);
        selectCountTextView.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);

        toggle.setDrawerIndicatorEnabled(true);
    }

    public void updateSelectCount() {
        selectCountTextView.setText(String.format("%d selected", selectCount));
    }
}

