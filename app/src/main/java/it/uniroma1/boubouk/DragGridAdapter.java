package it.uniroma1.boubouk;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DragGridAdapter extends BaseAdapter {
    private Context context;
    private List<Book> items;

    public DragGridAdapter(Context context, List<Book> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.grid_item, parent, false);

            holder = new ViewHolder();
            holder.coverImageView = row.findViewById(R.id.cover_image);
            holder.titleTextView = row.findViewById(R.id.title);
            holder.authorTextView = row.findViewById(R.id.author);
            row.setTag(holder);
        } else {
            holder=(ViewHolder)row.getTag();
        }

        Book book = items.get(position);
        holder.coverImageView.setImageDrawable(
                context.getResources().getDrawable(R.drawable.googleg_standard_color_18)
        );
        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());

        return row;
    }

    public List<Book> getItems() {
        return items;
    }

    static class ViewHolder {
        ImageView coverImageView;
        TextView titleTextView;
        TextView authorTextView;
    }
}
