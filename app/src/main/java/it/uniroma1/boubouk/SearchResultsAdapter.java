package it.uniroma1.boubouk;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import it.uniroma1.boubouk.classes.Book;
import it.uniroma1.boubouk.db.AppDatabase;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    private final ArrayList<Book> items;
    private AppDatabase db;
    private SearchOnlineActivity context;
    private LayoutInflater inflater;

    public SearchResultsAdapter(Context context, ArrayList<Book> items) {
        this.db = AppDatabase.getAppDatabase(context);
        this.items = items;
        this.context = (SearchOnlineActivity) context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public SearchResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Book book = items.get(i);

        new Util.DownloadImageTask(context, viewHolder.cover).execute(book.getImageUrl(), book.getId());
        viewHolder.titleTextView.setText(items.get(i).getTitle());
        viewHolder.authorTextView.setText(items.get(i).getAuthor());
        if (db.daoAccess().checkAlreadyOwned(db.getUser().getEmail(), book.getId()) != null) {
            viewHolder.addButton.setVisibility(View.INVISIBLE);
            viewHolder.alreadyAdded.setVisibility(View.VISIBLE);
        } else {
            viewHolder.addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!viewHolder.added) {
                        viewHolder.added = true;
                        viewHolder.addButton.setVisibility(View.INVISIBLE);
                        viewHolder.alreadyAdded.setVisibility(View.VISIBLE);
                        context.addBookToAdded(book);
                        Toast toast = Toast.makeText(
                                context,
                                context.getString(R.string.book_added),
                                Toast.LENGTH_SHORT
                        );
                        toast.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        boolean added = false;

        ImageView cover;
        TextView titleTextView;
        TextView authorTextView;
        TextView alreadyAdded;
        ImageView addButton;

        public ViewHolder(View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.result_cover);
            titleTextView = itemView.findViewById(R.id.result_title);
            authorTextView = itemView.findViewById(R.id.result_author);
            alreadyAdded = itemView.findViewById(R.id.already_added);
            addButton = itemView.findViewById(R.id.result_add);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(MainActivity.BOOK_EXTRA, items.get(getAdapterPosition()));
            context.startActivity(intent);
        }
    }
}
