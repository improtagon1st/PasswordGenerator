package com.example.passwordgenerator;        // ← замени, если у тебя другой package

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

/**
 * Главная (и единственная) активити генератора паролей.
 * Использует Material 3-виджеты и библиотеку zxcvbn для оценки стойкости.
 */
public class MainActivity extends AppCompatActivity {

    /* ---------- UI ---------- */

    private TextInputEditText etPassword;
    private Slider sliderLength;
    private MaterialCheckBox cbUpper, cbLower, cbDigits, cbSymbols;
    private LinearProgressIndicator progressStrength;
    private MaterialButton btnGenerate, btnCopy;

    /* ---------- Данные ---------- */

    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS  = "0123456789";
    private static final String SYMBOLS = "!@#$%&*()-_=+";

    private final SecureRandom random = new SecureRandom();
    private final Zxcvbn zxcvbn       = new Zxcvbn();

    /* ---------- Жизненный цикл ---------- */

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

        // Хотите выводить текущую длину в Toast при изменении слайдера? Раскомментируйте:
        /*
        sliderLength.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) Toast.makeText(this, "Длина: " + (int) value, Toast.LENGTH_SHORT).show();
        });
        */
    }

    /* ---------- Генерация и копирование ---------- */

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

    /* ---------- Логика генерации ---------- */

    /** Строит пароль нужной длины, гарантируя наличие символов всех выбранных наборов. */
    private String buildPassword(int length, List<String> sets) {
        List<Character> chars = new ArrayList<>();

        // 1) минимум по одному символу из каждого набора
        for (String s : sets) chars.add(randomChar(s));

        // 2) объединяем выборку
        StringBuilder poolBuilder = new StringBuilder();
        for (String s : sets) poolBuilder.append(s);
        String pool = poolBuilder.toString();

        // 3) заполняем оставшееся
        while (chars.size() < length) chars.add(randomChar(pool));

        // 4) перемешиваем
        Collections.shuffle(chars, random);

        // 5) формируем строку
        StringBuilder result = new StringBuilder();
        for (char c : chars) result.append(c);
        return result.toString();
    }

    private char randomChar(String src) {
        return src.charAt(random.nextInt(src.length()));
    }

    /* ---------- Оценка стойкости zxcvbn ---------- */

    private void updateStrengthIndicator(String password) {
        Strength s = zxcvbn.measure(password);  // score 0-4
        int score = s.getScore();               // чем больше, тем сильнее
        int progress = (score + 1) * 20;        // 20–100 %

        // Анимированно обновляем линейный прогресс
        progressStrength.setProgressCompat(progress, true);

        String[] labels = {"Очень слабый", "Слабый", "Средний", "Хороший", "Отличный"};
        Toast.makeText(this, "Сила: " + labels[score], Toast.LENGTH_SHORT).show();
    }
}
