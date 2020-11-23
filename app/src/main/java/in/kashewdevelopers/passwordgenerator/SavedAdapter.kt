package `in`.kashewdevelopers.passwordgenerator

import `in`.kashewdevelopers.passwordgenerator.databinding.SavedItemLayoutBinding
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter

class SavedAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {

    private var cursorInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return cursorInflater.inflate(R.layout.saved_item_layout, null)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val binding: SavedItemLayoutBinding = SavedItemLayoutBinding.bind(view as View)

        val password = cursor?.getString(cursor.getColumnIndex(SavedDbHelper.PSWD))
        binding.password.text = password

        binding.parent.setOnClickListener {
            onPasswordClickListener?.onPasswordClickListener(password ?: "")
        }

        binding.deleteIcon.setOnClickListener {
            onDeleteClickListener?.onDeleteClickListener(password ?: "")
        }
    }


    fun setOnDeleteClickListener(onDeleteClickListener: OnDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener
    }

    fun setOnPasswordClickListener(onPasswordClickListener: OnPasswordClickListener) {
        this.onPasswordClickListener = onPasswordClickListener
    }

    interface OnDeleteClickListener {
        fun onDeleteClickListener(password: String)
    }

    interface OnPasswordClickListener {
        fun onPasswordClickListener(password: String)
    }

    private var onDeleteClickListener: OnDeleteClickListener? = null
    private var onPasswordClickListener: OnPasswordClickListener? = null

}