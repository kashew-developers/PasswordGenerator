package in.kashewdevelopers.passwordgenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
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
            Toast.makeText(this, "Open Saved", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    // functionality
    @SuppressLint("ShowToast")
    public void initialize() {
        password = findViewById(R.id.password);
        controlIcon = findViewById(R.id.controlIcon);
        controls = findViewById(R.id.controls);
        passwordLengthChanger = findViewById(R.id.passwordLength);
        passwordLengthTV = findViewById(R.id.passwordLengthCount);

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

        controls.setVisibility(View.GONE);

        // toasts
        pressBackAgainToast = Toast.makeText(this, R.string.back_press, Toast.LENGTH_SHORT);
        pressBackAgainToast.setGravity(Gravity.CENTER, 0, 0);

        savedToast = Toast.makeText(this, R.string.saved_password, Toast.LENGTH_SHORT);
        savedToast.setGravity(Gravity.CENTER, 0, 0);

        copiedToast = Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT);
        copiedToast.setGravity(Gravity.CENTER, 0, 0);

        copyErrorToast = Toast.makeText(this, R.string.copy_error, Toast.LENGTH_SHORT);
        copyErrorToast.setGravity(Gravity.CENTER, 0, 0);
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

}
