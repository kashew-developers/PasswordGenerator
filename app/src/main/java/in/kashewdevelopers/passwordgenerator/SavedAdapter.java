package in.kashewdevelopers.passwordgenerator;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SavedAdapter extends CursorAdapter {

    private LayoutInflater cursorInflator;

    SavedAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        cursorInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return cursorInflator.inflate(R.layout.saved_item_layout, null);
    }


    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // initialize widgets
        LinearLayout parent = view.findViewById(R.id.parent);
        TextView passwordTV = view.findViewById(R.id.password);
        ImageView deleteIcon = view.findViewById(R.id.delete_icon);

        // get data from cursor
        final String password = cursor.getString(cursor.getColumnIndex(SavedDbHelper.PSWD));

        passwordTV.setText(password);

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPasswordClickListener != null) {
                    onPasswordClickListener.onPasswordClickListener(password);
                }
            }
        });

        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClickListener(password);
                }
            }
        });
    }


    void setOnPasswordClickListener(OnPasswordClickListener onPasswordClickListener) {
        this.onPasswordClickListener = onPasswordClickListener;
    }

    void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    interface OnDeleteClickListener {
        void onDeleteClickListener(String password);
    }

    interface OnPasswordClickListener {
        void onPasswordClickListener(String password);
    }

    private OnPasswordClickListener onPasswordClickListener;
    private OnDeleteClickListener onDeleteClickListener;

}