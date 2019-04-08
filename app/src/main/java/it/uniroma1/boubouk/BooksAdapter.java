package it.uniroma1.boubouk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import it.uniroma1.boubouk.classes.Book;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.ViewHolder> implements Filterable {
    private ArrayList<Book> originalItems;
    private ArrayList<Book> filteredItems;
    private HashMap<String, Boolean> selected;
    private Comparator<Book> compAuthor;
    private Comparator<Book> compTitle;
    private ItemFilter filter;
    private LayoutInflater inflater;
    private MainActivity context;
    private RecyclerView recyclerView;

    BooksAdapter(MainActivity context, ArrayList<Book> items, RecyclerView recyclerView) {
        this.originalItems = items;
        this.filteredItems = new ArrayList<>(items);
        this.selected = new HashMap<>();
        for (Book book : items) {
            setSelected(book.getId(), false);
        }
        this.compAuthor = new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                if (o1.getAuthor().equals(o2.getAuthor())) {
                    return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                }
                return o1.getAuthor().compareToIgnoreCase(o2.getAuthor());

            }
        };
        this.compTitle = new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                if (o1.getTitle().equals(o2.getTitle())) {
                    return o1.getAuthor().compareToIgnoreCase(o2.getAuthor());
                }
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
        };
        this.context = context;
        this.filter = new ItemFilter();
        this.inflater = LayoutInflater.from(context);
        this.recyclerView = recyclerView;
    }


    void deselect(String id) {
        if (getSelected(id)) {
            setSelected(id, false);
            context.removeSelectCount();
            context.updateSelectCount();
        }


        BooksAdapter.ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForItemId(id.hashCode());
        if (viewHolder != null) {
            ((CardView) viewHolder.itemView)
                    .setCardBackgroundColor(context.getResources().getColor(R.color.colorWhite));
        }

        if (context.getSelectCount() == 0) {
            context.disableSelectMode();
        }
    }

    void select(String id) {
        if (!getSelected(id)) {
            setSelected(id, true);
            context.addSelectCount();
            context.updateSelectCount();
        }

        BooksAdapter.ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForItemId(id.hashCode());
        if (viewHolder != null) {
            ((CardView) viewHolder.itemView)
                    .setCardBackgroundColor(context.getResources().getColor(R.color.colorSelected));
        }

        if (context.getSelectCount() > 0) {
            context.setupSelectMode();
        }
    }

    @NonNull
    @Override
    public BooksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_book, parent, false);
        return new BooksAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BooksAdapter.ViewHolder viewHolder, int i) {
        viewHolder.pos = viewHolder.getAdapterPosition();
        viewHolder.book = filteredItems.get(viewHolder.pos);
        viewHolder.titleTextView.setText(viewHolder.book.getTitle());
        viewHolder.authorTextView.setText(viewHolder.book.getAuthor());

        new Util.DownloadImageTask(context, viewHolder.cover).execute(viewHolder.book.getImageUrl(), viewHolder.book.getId());

        if (getSelected(viewHolder.book.getId())) {
            ((CardView) viewHolder.itemView).setCardBackgroundColor(context.getResources().getColor(R.color.colorSelected));
        } else {
            ((CardView) viewHolder.itemView).setCardBackgroundColor(context.getResources().getColor(R.color.colorWhite));
        }
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    @Override
    public long getItemId(int position) {
        return originalItems.get(position).getId().hashCode();
    }

    ArrayList<Book> getFilteredItems() {
        return filteredItems;
    }

    boolean getSelected(String id) {
        return selected.get(id);
    }

    private void setSelected(String id, boolean val) {
        selected.put(id, val);
    }

    void add(ArrayList<Book> books) {
        for (Book book : books) {
            if (!originalItems.contains(book)) {
                originalItems.addAll(books);
            }
            if (!filteredItems.contains(book)) {
                filteredItems.addAll(books);
            }
            setSelected(book.getId(), false);
        }
        notifyDataSetChanged();
    }

    void remove(Book book) {
        originalItems.remove(book);
        filteredItems.remove(book);
        notifyDataSetChanged();
    }

    void sort(boolean sortedByAuthor, boolean notify) {
        Collections.sort(
                originalItems,
                sortedByAuthor ? compAuthor : compTitle
        );
        Collections.sort(
                filteredItems,
                sortedByAuthor ? compAuthor : compTitle
        );
        if (notify) {
            notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        Book book;
        ImageView cover;
        TextView titleTextView;
        TextView authorTextView;
        int pos;

        ViewHolder(View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.cover_image);
            titleTextView = itemView.findViewById(R.id.title);
            authorTextView = itemView.findViewById(R.id.author);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if ((context).getSelectCount() > 0) {
                if (getSelected(book.getId())) {
                    deselect(book.getId());
                    //((CardView) v).setCardBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                } else {
                    select(book.getId());
                    //((CardView) v).setCardBackgroundColor(context.getResources().getColor(R.color.colorSelected));
                }
            } else {
                Intent intent = new Intent(context, DetailActivity.class);
                Book book = filteredItems.get(getAdapterPosition());
                intent.putExtra(MainActivity.BOOK_EXTRA, book);
                context.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (getSelected(book.getId())) {
                deselect(book.getId());
                //((CardView) v).setCardBackgroundColor(context.getResources().getColor(R.color.colorWhite));
                return true;
            } else {
                select(book.getId());
                return true;
                //((CardView) v).setCardBackgroundColor(context.getResources().getColor(R.color.colorSelected));
            }
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            final List<Book> originalList = originalItems;
            final ArrayList<Book> filteredAfterPerform = new ArrayList<>(originalList.size());

            for (Book book : originalList) {
                if (Util.matchesSearch(book.getTitle().toLowerCase(), filterString) ||
                        Util.matchesSearch(book.getAuthor().toLowerCase(), filterString)) {
                    filteredAfterPerform.add(book);
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredAfterPerform;
            results.count = filteredAfterPerform.size();

            if (filteredItems.size() > results.count) {
                filteredItems.retainAll(filteredAfterPerform);
            } else {
                for (Book newBook : filteredAfterPerform) {
                    if (!filteredItems.contains(newBook)) {
                        filteredItems.add(newBook);
                    }
                }
            }
            sort(context.isSortedByAuthor(), false);
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyDataSetChanged();
        }

    }

}
