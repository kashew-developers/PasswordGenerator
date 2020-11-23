package `in`.kashewdevelopers.passwordgenerator

import `in`.kashewdevelopers.passwordgenerator.databinding.ActivityMainBinding
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // control variables
    private var minPasswordLength = 5
    private var passwordLength = 8
    private var includeUppercase = true
    private var includeLowercase = true
    private var includeNumbers = true
    private var includeSymbols = true

    // character set
    private var uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private var lowercaseLetters = "abcdefghijklmnopqrstuvwxyz"
    private var numbers = "0123456789"
    private var symbols = "!\"#$%&\'()*+,-./:;<=>?@[\\]^_`{|}"

    // toasts
    private lateinit var pressBackAgainToast: Toast
    private lateinit var savedToast: Toast
    private lateinit var copiedToast: Toast
    private lateinit var copyErrorToast: Toast
    private var backPressed = false

    // suggestion db
    private lateinit var dbHelper: SavedDbHelper
    private lateinit var db: SQLiteDatabase
    private var adapter: SavedAdapter? = null
    private lateinit var cursor: Cursor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()

        generatePassword()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.saved) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (backPressed) {
                pressBackAgainToast.cancel()
                savedToast.cancel()
                copiedToast.cancel()
                copyErrorToast.cancel()

                super.onBackPressed()
                return
            }

            backPressed = true
            pressBackAgainToast.show()

            Handler().postDelayed(Runnable { backPressed = false }, 2000)
        }
    }


    // functionality
    private fun initialize() {
        initializeToasts()
        initializeDb()

        binding.controls.visibility = View.GONE

        drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout,
                R.string.drawer_open, R.string.drawer_close)
        binding.drawerLayout.addDrawerListener(drawerToggle)

        setPasswordLengthChangeListener()
        setPasswordDeleteListener()
        setPasswordClickListener()
    }

    @SuppressLint("ShowToast")
    private fun initializeToasts() {
        pressBackAgainToast = Toast.makeText(this, R.string.back_press, Toast.LENGTH_SHORT)
        pressBackAgainToast.setGravity(Gravity.CENTER, 0, 0)

        savedToast = Toast.makeText(this, R.string.saved_password, Toast.LENGTH_SHORT)
        savedToast.setGravity(Gravity.CENTER, 0, 0)

        copiedToast = Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT)
        copiedToast.setGravity(Gravity.CENTER, 0, 0)

        copyErrorToast = Toast.makeText(this, R.string.copy_error, Toast.LENGTH_SHORT)
        copyErrorToast.setGravity(Gravity.CENTER, 0, 0)
    }

    private fun initializeDb() {
        dbHelper = SavedDbHelper(this)

        db = dbHelper.readableDatabase
        cursor = dbHelper.get(db)

        adapter = SavedAdapter(this, cursor)

        binding.savedList.adapter = adapter

        if (cursor.count == 0) {
            binding.noSavedPswd.visibility = View.VISIBLE
            binding.clearSaved.visibility = View.GONE
        } else {
            binding.noSavedPswd.visibility = View.GONE
            binding.clearSaved.visibility = View.VISIBLE
        }
    }

    private fun generatePassword() {
        var charSet = ""
        if (includeUppercase) charSet += uppercaseLetters
        if (includeLowercase) charSet += lowercaseLetters
        if (includeNumbers) charSet += numbers
        if (includeSymbols) charSet += symbols

        if (charSet.isEmpty()) {
            return
        }

        val random = Random(System.currentTimeMillis())

        val randomString: StringBuilder = StringBuilder(passwordLength)
        for (i in 0 until passwordLength) {
            randomString.append(charSet[random.nextInt(charSet.length)])
        }

        binding.password.text = randomString
    }

    private fun deleteAllSavedPasswords() {
        dbHelper.deleteAll(db)

        cursor = dbHelper.get(db)
        adapter?.swapCursor(cursor)
        adapter?.notifyDataSetChanged()

        if (cursor.count == 0) {
            binding.noSavedPswd.visibility = View.VISIBLE
            binding.clearSaved.visibility = View.GONE
        } else {
            binding.noSavedPswd.visibility = View.GONE
            binding.clearSaved.visibility = View.VISIBLE
        }
    }

    private fun deleteSavedPasswords(password: String) {
        dbHelper.delete(db, password)

        cursor = dbHelper.get(db)
        adapter?.swapCursor(cursor)
        adapter?.notifyDataSetChanged()

        if (cursor.count == 0) {
            binding.noSavedPswd.visibility = View.VISIBLE
            binding.clearSaved.visibility = View.GONE
        } else {
            binding.noSavedPswd.visibility = View.GONE
            binding.clearSaved.visibility = View.VISIBLE
        }
    }

    private fun showPassword(password: String) {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        binding.password.text = password
    }


    // handle widget clicks
    fun copyClicked(view: View) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText(getText(R.string.app_name), binding.password.text)
        clipboard.setPrimaryClip(clip)

        copiedToast.show()
    }

    fun saveClicked(view: View) {
        val password: String = binding.password.text.toString()
        dbHelper.insert(db, password)

        cursor = dbHelper.get(db)
        adapter?.swapCursor(cursor)
        adapter?.notifyDataSetChanged()

        savedToast.show()

        if (cursor.count == 0) {
            binding.noSavedPswd.visibility = View.VISIBLE
            binding.clearSaved.visibility = View.GONE
        } else {
            binding.noSavedPswd.visibility = View.GONE
            binding.clearSaved.visibility = View.VISIBLE
        }
    }

    fun shareClicked(view: View) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, binding.password.text.toString())
        startActivity(Intent.createChooser(intent, getText(R.string.share_with)))
    }

    fun controlToggleClicked(view: View) {
        if (binding.controls.visibility == View.GONE) {
            binding.controlIcon.setImageResource(R.drawable.close_icon)
            binding.controls.visibility = View.VISIBLE
        } else {
            binding.controlIcon.setImageResource(R.drawable.settings_icon)
            binding.controls.visibility = View.GONE
        }
    }

    fun refreshClicked(view: View) = generatePassword()

    fun uppercaseClicked(view: View) {
        includeUppercase = (view as CheckBox).isChecked
        generatePassword()
    }

    fun lowercaseClicked(view: View) {
        includeLowercase = (view as CheckBox).isChecked
        generatePassword()
    }

    fun numbersClicked(view: View) {
        includeNumbers = (view as CheckBox).isChecked
        generatePassword()
    }

    fun symbolsClicked(view: View) {
        includeSymbols = (view as CheckBox).isChecked
        generatePassword()
    }

    fun clearSavedClicked(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.delete_all)
        builder.setMessage(R.string.are_u_sure)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.delete, DialogInterface.OnClickListener { _, _ -> deleteAllSavedPasswords() })
        builder.show()
    }


    // listeners
    private fun setPasswordLengthChangeListener() {
        binding.passwordLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.passwordLengthCount.text = "${progress + minPasswordLength}"
                passwordLength = minPasswordLength + progress
                generatePassword()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun setPasswordDeleteListener() {
        adapter?.setOnDeleteClickListener(object : SavedAdapter.OnDeleteClickListener {
            override fun onDeleteClickListener(password: String) {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle(R.string.delete)
                builder.setMessage(R.string.are_u_sure)
                builder.setNegativeButton(R.string.cancel, null)
                builder.setPositiveButton(R.string.delete) { _, _ -> deleteSavedPasswords(password) }
                builder.show()
            }
        })
    }

    private fun setPasswordClickListener() {
        adapter?.setOnPasswordClickListener(object : SavedAdapter.OnPasswordClickListener {
            override fun onPasswordClickListener(password: String) {
                deleteSavedPasswords(password)
            }
        })
    }

}