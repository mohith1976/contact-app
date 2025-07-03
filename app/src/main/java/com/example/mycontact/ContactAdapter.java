package com.example.mycontact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contactList;

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }
    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPhone;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textPhone = itemView.findViewById(R.id.textPhone);
        }
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.textName.setText(contact.getName());
        holder.textPhone.setText(contact.getNumber());


        holder.textName.setText(contact.getName());
        holder.textPhone.setText(contact.getNumber());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            }
        });
        holder.itemView.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Choose an option")
                    .setItems(new CharSequence[]{"Update", "Delete"}, (dialog, which) -> {
                        if (which == 0) {
                            if (listener != null) {
                                listener.onContactClick(contact);
                            }
                        } else if (which == 1) {
                            if (longClickListener != null) {
                                longClickListener.onContactLongClick(contact);
                            }
                        }
                    });
            builder.show();
            return true;
        });


    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    private OnContactClickListener listener;

    public ContactAdapter(List<Contact> contactList, OnContactClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }
    public interface OnContactLongClickListener {
        void onContactLongClick(Contact contact);
    }

    private OnContactLongClickListener longClickListener;

    public void setOnContactLongClickListener(OnContactLongClickListener listener) {
        this.longClickListener = listener;
    }


}
