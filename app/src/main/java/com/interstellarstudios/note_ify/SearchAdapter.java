package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.interstellarstudios.note_ify.database.NoteEntity;
import java.util.List;
import jp.wasabeef.richeditor.RichEditor;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private Context context;
    private List<NoteEntity> NoteList;
    private boolean switchPriorityOnOff;
    private boolean switchThemesOnOff;

    class SearchViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        RichEditor mEditor;
        TextView textViewPriority;
        TextView textViewDate;
        TextView textViewFromUserEmail;
        TextView textViewRevision;
        ImageView attachment_icon;
        CardView cardView;
        TextView attachmentName;
        ImageView playIcon;
        TextView playText;
        View parentView;

        SearchViewHolder(View itemView) {
            super(itemView);

            String colorLightThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorLightThemeText));
            String colorLightThemeCardBackgroundString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.cardBackground));
            mEditor = itemView.findViewById(R.id.mEditor);
            mEditor.setInputEnabled(false);
            mEditor.setBackgroundColor(Color.parseColor(colorLightThemeCardBackgroundString));
            mEditor.setEditorFontColor(Color.parseColor(colorLightThemeTextString));

            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewPriority = itemView.findViewById(R.id.text_view_priority);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewFromUserEmail = itemView.findViewById(R.id.fromUserEmail);
            textViewRevision = itemView.findViewById(R.id.revision);
            attachment_icon = itemView.findViewById(R.id.attachment_icon);
            cardView = itemView.findViewById(R.id.cardView);
            attachmentName = itemView.findViewById(R.id.attachmentName);
            playIcon = itemView.findViewById(R.id.audio_icon);
            playText = itemView.findViewById(R.id.audio_text);

            parentView = itemView;
        }
    }

    public SearchAdapter(List<NoteEntity> NoteList, SharedPreferences sharedPreferences) {

        switchPriorityOnOff = sharedPreferences.getBoolean("switchPriorityColor", false);
        switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);
        this.NoteList = NoteList;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        NoteEntity currentItem = NoteList.get(position);

        if (switchThemesOnOff) {

            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            holder.textViewTitle.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewDate.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewFromUserEmail.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewRevision.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewPriority.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardBackgroundDarkTheme));

            String colorDarkThemeCardBackgroundString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.cardBackgroundDarkTheme));
            holder.mEditor.setBackgroundColor(Color.parseColor(colorDarkThemeCardBackgroundString));
            holder.mEditor.setEditorFontColor(Color.parseColor(colorDarkThemeTextString));
        }

        holder.attachmentName.setVisibility(View.GONE);
        holder.attachment_icon.setVisibility(View.GONE);
        holder.playIcon.setVisibility(View.GONE);
        holder.playText.setVisibility(View.GONE);

        holder.textViewTitle.setText(currentItem.getTitle());

        String fullDescription = currentItem.getDescription();
        if (fullDescription != null) {
            String shortDescription;
            if (fullDescription.length() > 100) {
                shortDescription = fullDescription.substring(0, 100).trim() + "...";
            } else {
                shortDescription = fullDescription;
            }
            holder.mEditor.setHtml(shortDescription);
        }

        if (switchPriorityOnOff) {
            int priority = currentItem.getPriority();
            if (priority >= 1 && priority <= 3) {
                holder.textViewPriority.setText("Priority: " + priority);
            } else if (priority >= 4 && priority <= 5) {
                holder.textViewPriority.setTextColor(Color.GREEN);
                holder.textViewPriority.setText("Priority: " + priority);
            } else if (priority >= 6 && priority <= 8) {
                holder.textViewPriority.setTextColor(Color.YELLOW);
                holder.textViewPriority.setText("Priority: " + priority);
            } else {
                holder.textViewPriority.setTextColor(Color.RED);
                holder.textViewPriority.setText("Priority: " + priority);
            }
        } else {
            holder.textViewPriority.setText("Priority: " + currentItem.getPriority());
        }

        holder.textViewDate.setText(currentItem.getDate());
        holder.textViewFromUserEmail.setText(currentItem.getFromEmailAddress());
        holder.textViewRevision.setText("Revision: " + currentItem.getRevision());
        holder.attachmentName.setText(currentItem.getAttachmentName());

        String attachmentURL = currentItem.getAttachmentUrl();
        if (attachmentURL != null && !attachmentURL.equals("")) {

            holder.attachmentName.setVisibility(View.VISIBLE);
            holder.attachmentName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(attachmentURL));
                    context.startActivity(browserIntent);
                }
            });

            holder.attachment_icon.setVisibility(View.VISIBLE);
            holder.attachment_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(attachmentURL));
                    context.startActivity(browserIntent);
                }
            });
        }

        String audioDownloadUrl = currentItem.getAudioUrl();
        if (audioDownloadUrl != null && !audioDownloadUrl.equals("")) {

            holder.playIcon.setVisibility(View.VISIBLE);
            holder.playIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(audioDownloadUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setDataAndType(Uri.parse(audioDownloadUrl), "audio/*");
                    context.startActivity(intent);
                }
            });

            holder.playText.setVisibility(View.VISIBLE);
            holder.playText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(audioDownloadUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setDataAndType(Uri.parse(audioDownloadUrl), "audio/*");
                    context.startActivity(intent);
                }
            });
        }

        holder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, EditNote.class);
                i.putExtra("folderId", currentItem.getFolderId());
                i.putExtra("noteId", currentItem.getNoteId());
                i.putExtra("title", currentItem.getTitle());
                i.putExtra("description", currentItem.getDescription());
                i.putExtra("priority", currentItem.getPriority());
                i.putExtra("revision", currentItem.getRevision());
                i.putExtra("attachmentUrl", attachmentURL);
                i.putExtra("attachmentName", currentItem.getAttachmentName());
                i.putExtra("audioDownloadUrl", audioDownloadUrl);
                i.putExtra("audioZipDownloadUrl", currentItem.getAudioZipUrl());
                i.putExtra("audioZipFileName", currentItem.getAudioZipName());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return NoteList.size();
    }
}
