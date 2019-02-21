package spe.uoblibraryapp;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import spe.uoblibraryapp.api.wmsobjects.WMSLoan;

public class LoanBookListAdapter extends ArrayAdapter<WMSLoan> {

    private Context mContext;
    int mResource;

    public LoanBookListAdapter(Context context, int resource, List<WMSLoan> objects){
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String title = getItem(position).getBook().getTitle();
        String author = getItem(position).getBook().getAuthor();
        Boolean overdue = getItem(position).isOverdue();
        Boolean willAutoRenew = getItem(position).getRenewable();
        Date dueDate = getItem(position).getDueDate();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String strDueDate = dateFormat.format(dueDate);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false );
        TextView textViewTitle = convertView.findViewById(R.id.txtTitle);
        TextView textViewAuthor = convertView.findViewById(R.id.txtAuthor);
        TextView textViewStatus = convertView.findViewById(R.id.txtStatus);

        textViewTitle.setText(title);
        textViewAuthor.setText(author);

        //TODO: CHECK IF SORTING WORKS ON DUE DATE!!!
        if (overdue) {
            textViewStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.colorOverdue));
            textViewStatus.setText("Overdue");
        } else if (willAutoRenew == null){
            textViewStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReservation));
            textViewStatus.setText("Fetching...");
        } else {
            if (!willAutoRenew) {
                textViewStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReservation));
                Date dateToday = new Date();
                int bookDueDate = daysBetween(dateToday, dueDate);
                if (bookDueDate <= 3)
                    if (bookDueDate == 1)
                        textViewStatus.setText(String.format("Due tomorrow", bookDueDate));
                    else
                        textViewStatus.setText(String.format("Due in %d days", bookDueDate));
                else
                    textViewStatus.setText("Due: " + strDueDate);
            } else {
                textViewStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.colorLoan));
                textViewStatus.setText("Will auto-renew");
            }
        }
        return convertView;
    }

    public int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

}
