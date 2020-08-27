package in.kashewdevelopers.passwordgenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // widgets
    TextView password;
    ImageView controlIcon;
    ConstraintLayout controls;
    SeekBar passwordLengthChanger;
    TextView passwordLengthTV;

    // navigation drawer elements
    DrawerLayout drawerLayout;
    ImageView clearListButton;
    TextView noSavedPasswords;
    ListView savedList;
    ConstraintLayout navigationDrawer;
    ActionBarDrawerToggle drawerToggle;

    // control variables
    int minPasswordLength = 5;
    int passwordLength = 8;
    boolean includeUppercase = true;
    boolean includeLowercase = true;
    boolean includeNumbers = true;
    boolean includeSymbols = true;

    // character set
    String uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
    String numbers = "0123456789";
    String symbols = "!\"#$%&\'()*+,-./:;<=>?@[\\]^_`{|}";

    // toasts
    Toast pressBackAgainToast, savedToast, copiedToast, copyErrorToast;
    boolean backPressed = false;

    // suggestion db
    private SavedDbHelper dbHelper;
    private SQLiteDatabase db;
    SavedAdapter adapter;
    Cursor cursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        generatePassword();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.saved) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else
                drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (backPressed) {
                pressBackAgainToast.cancel();
                savedToast.cancel();
                copiedToast.cancel();
                copyErrorToast.cancel();

                super.onBackPressed();
                return;
            }

            backPressed = true;
            pressBackAgainToast.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressed = false;
                }
            }, 2000);
        }
    }


    // functionality
    public void initialize() {
        initializeWidgets();
        initializeToasts();
        initializeDb();

        controls.setVisibility(View.GONE);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        setPasswordLengthChangeListener();
        setPasswordDeleteListener();
        setPasswordClickListener();
    }

    public void initializeWidgets() {
        password = findViewById(R.id.password);
        controlIcon = findViewById(R.id.controlIcon);
        controls = findViewById(R.id.controls);
        passwordLengthChanger = findViewById(R.id.passwordLength);
        passwordLengthTV = findViewById(R.id.passwordLengthCount);

        // navigation ui
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationDrawer = findViewById(R.id.slider);
        clearListButton = findViewById(R.id.clear_saved);
        noSavedPasswords = findViewById(R.id.no_saved_pswd);
        savedList = findViewById(R.id.saved_list);
    }

    @SuppressLint("ShowToast")
    public void initializeToasts() {
        pressBackAgainToast = Toast.makeText(this, R.string.back_press, Toast.LENGTH_SHORT);
        pressBackAgainToast.setGravity(Gravity.CENTER, 0, 0);

        savedToast = Toast.makeText(this, R.string.saved_password, Toast.LENGTH_SHORT);
        savedToast.setGravity(Gravity.CENTER, 0, 0);

        copiedToast = Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT);
        copiedToast.setGravity(Gravity.CENTER, 0, 0);

        copyErrorToast = Toast.makeText(this, R.string.copy_error, Toast.LENGTH_SHORT);
        copyErrorToast.setGravity(Gravity.CENTER, 0, 0);
    }

    public void initializeDb() {
        dbHelper = new SavedDbHelper(this);
        try {
            db = dbHelper.getReadableDatabase();
            cursor = dbHelper.get(db);

            adapter = new SavedAdapter(this, cursor, 0);

            savedList.setAdapter(adapter);

            if (cursor.getCount() == 0) {
                noSavedPasswords.setVisibility(View.VISIBLE);
                clearListButton.setVisibility(View.GONE);
            } else {
                noSavedPasswords.setVisibility(View.GONE);
                clearListButton.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            db = null;
            Log.d("KashewDevelopers", "Error : " + e.getMessage());
        }
    }

    public void generatePassword() {
        String charSet = "";
        if (includeUppercase) charSet += uppercaseLetters;
        if (includeLowercase) charSet += lowercaseLetters;
        if (includeNumbers) charSet += numbers;
        if (includeSymbols) charSet += symbols;

        if (charSet.isEmpty()) {
            return;
        }

        Random random = new Random(System.currentTimeMillis());

        StringBuilder randomString = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            randomString.append(charSet.charAt(random.nextInt(charSet.length())));
        }

        password.setText(randomString);
    }

    public void deleteAllSavedPasswords() {
        if (dbHelper == null || db == null)
            return;

        dbHelper.deleteAll(db);

        cursor = dbHelper.get(db);
        adapter.swapCursor(cursor);
        adapter.notifyDataSetChanged();

        if (cursor.getCount() == 0) {
            noSavedPasswords.setVisibility(View.VISIBLE);
            clearListButton.setVisibility(View.GONE);
        } else {
            noSavedPasswords.setVisibility(View.GONE);
            clearListButton.setVisibility(View.VISIBLE);
        }
    }

    public void deleteSavedPasswords(String password) {
        if (dbHelper == null || db == null)
            return;

        dbHelper.delete(db, password);

        cursor = dbHelper.get(db);
        adapter.swapCursor(cursor);
        adapter.notifyDataSetChanged();

        if (cursor.getCount() == 0) {
            noSavedPasswords.setVisibility(View.VISIBLE);
            clearListButton.setVisibility(View.GONE);
        } else {
            noSavedPasswords.setVisibility(View.GONE);
            clearListButton.setVisibility(View.VISIBLE);
        }
    }

    public void showPassword(String pass) {
        drawerLayout.closeDrawer(GravityCompat.START);
        password.setText(pass);
    }


    // handle widget clicks
    public void copyClicked(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard == null) {
            copyErrorToast.show();
            return;
        }

        ClipData clip = ClipData.newPlainText(getText(R.string.app_name), password.getText());
        clipboard.setPrimaryClip(clip);
        copyErrorToast.cancel();
        copiedToast.show();
    }

    public void saveClicked(View view) {
        if (dbHelper == null || db == null)
            return;

        String pswd = password.getText().toString();
        dbHelper.insert(db, pswd);

        cursor = dbHelper.get(db);
        adapter.swapCursor(cursor);
        adapter.notifyDataSetChanged();

        savedToast.show();

        if (cursor.getCount() == 0) {
            noSavedPasswords.setVisibility(View.VISIBLE);
            clearListButton.setVisibility(View.GONE);
        } else {
            noSavedPasswords.setVisibility(View.GONE);
            clearListButton.setVisibility(View.VISIBLE);
        }
    }

    public void shareClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, password.getText());
        startActivity(Intent.createChooser(intent, getText(R.string.share_with)));
    }

    public void controlToggleClicked(View view) {
        if (controls.getVisibility() == View.GONE) {
            controlIcon.setImageResource(R.drawable.close_icon);
            controls.setVisibility(View.VISIBLE);
        } else {
            controlIcon.setImageResource(R.drawable.settings_icon);
            controls.setVisibility(View.GONE);
        }
    }

    public void refreshClicked(View view) {
        generatePassword();
    }

    public void uppercaseClicked(View view) {
        includeUppercase = ((CheckBox) view).isChecked();
        generatePassword();
    }

    public void lowercaseClicked(View view) {
        includeLowercase = ((CheckBox) view).isChecked();
        generatePassword();
    }

    public void numbersClicked(View view) {
        includeNumbers = ((CheckBox) view).isChecked();
        generatePassword();
    }

    public void symbolsClicked(View view) {
        includeSymbols = ((CheckBox) view).isChecked();
        generatePassword();
    }

    public void clearSavedClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_all);
        builder.setMessage(R.string.are_u_sure);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllSavedPasswords();
            }
        });
        builder.show();
    }


    // listeners
    public void setPasswordLengthChangeListener() {
        passwordLengthChanger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordLengthTV.setText(String.valueOf(progress + minPasswordLength));
                passwordLength = minPasswordLength + progress;
                generatePassword();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void setPasswordDeleteListener() {
        adapter.setOnDeleteClickListener(new SavedAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClickListener(final String password) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.delete);
                builder.setMessage(R.string.are_u_sure);
                builder.setNegativeButton(R.string.cancel, null);
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSavedPasswords(password);
                    }
                });
                builder.show();
            }
        });
    }

    public void setPasswordClickListener() {
        adapter.setOnPasswordClickListener(new SavedAdapter.OnPasswordClickListener() {
            @Override
            public void onPasswordClickListener(String password) {
                showPassword(password);
            }
        });
    }

}
