package com.example.passwordgenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private TextInputEditText etPassword;
    private Slider sliderLength;
    private MaterialCheckBox cbUpper, cbLower, cbDigits, cbSymbols;
    private LinearProgressIndicator progressStrength;
    private MaterialButton btnGenerate, btnCopy;

    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS  = "0123456789";
    private static final String SYMBOLS = "!@#$%&*()-_=+";

    private final SecureRandom random = new SecureRandom();
    private final Zxcvbn zxcvbn       = new Zxcvbn();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setListeners();
    }

    private void bindViews() {
        etPassword       = findViewById(R.id.etPassword);
        sliderLength     = findViewById(R.id.sliderLength);
        cbUpper          = findViewById(R.id.cbUpper);
        cbLower          = findViewById(R.id.cbLower);
        cbDigits         = findViewById(R.id.cbDigits);
        cbSymbols        = findViewById(R.id.cbSymbols);
        progressStrength = findViewById(R.id.progressStrength);
        btnGenerate      = findViewById(R.id.btnGenerate);
        btnCopy          = findViewById(R.id.btnCopy);
    }

    private void setListeners() {
        btnGenerate.setOnClickListener(v -> generateAndShow());
        btnCopy.setOnClickListener(v -> copyToClipboard());


        sliderLength.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) Toast.makeText(this, "Длина: " + (int) value, Toast.LENGTH_SHORT).show();
        });
    }



    private void generateAndShow() {
        int length = Math.round(sliderLength.getValue());

        List<String> sets = new ArrayList<>();
        if (cbUpper.isChecked())   sets.add(UPPER);
        if (cbLower.isChecked())   sets.add(LOWER);
        if (cbDigits.isChecked())  sets.add(DIGITS);
        if (cbSymbols.isChecked()) sets.add(SYMBOLS);

        if (sets.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы один набор символов", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = buildPassword(length, sets);
        etPassword.setText(password);

        updateStrengthIndicator(password);
    }

    private void copyToClipboard() {
        String pwd = String.valueOf(etPassword.getText());
        if (pwd.isEmpty()) {
            Toast.makeText(this, "Сначала сгенерируйте пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("password", pwd));
        Toast.makeText(this, "Пароль скопирован в буфер", Toast.LENGTH_SHORT).show();
    }


    private String buildPassword(int length, List<String> sets) {
        List<Character> chars = new ArrayList<>();

        for (String s : sets) chars.add(randomChar(s));

        StringBuilder poolBuilder = new StringBuilder();
        for (String s : sets) poolBuilder.append(s);
        String pool = poolBuilder.toString();

        while (chars.size() < length) chars.add(randomChar(pool));

        Collections.shuffle(chars, random);

        StringBuilder result = new StringBuilder();
        for (char c : chars) result.append(c);
        return result.toString();
    }

    private char randomChar(String src) {
        return src.charAt(random.nextInt(src.length()));
    }

    private void updateStrengthIndicator(String password) {
        Strength s = zxcvbn.measure(password);
        int score = s.getScore();
        int progress = (score + 1) * 20;

        progressStrength.setProgressCompat(progress, true);

        String[] labels = {"Очень слабый", "Слабый", "Средний", "Хороший", "Отличный"};
        Toast.makeText(this, "Сила: " + labels[score], Toast.LENGTH_SHORT).show();
    }
}
