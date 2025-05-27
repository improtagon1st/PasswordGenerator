package com.example.passwordgenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI
    private EditText etPassword;
    private TextView tvLengthLabel, tvStrength;
    private SeekBar seekLength;
    private CheckBox cbUpper, cbLower, cbDigits, cbSymbols;
    private ProgressBar progressStrength;
    private Button btnGenerate, btnCopy;

    // Символьные наборы
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%&*()-_=+";

    private final SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Привязываем view
        etPassword       = findViewById(R.id.etPassword);
        tvLengthLabel    = findViewById(R.id.tvLengthLabel);
        tvStrength       = findViewById(R.id.tvStrength);
        seekLength       = findViewById(R.id.seekLength);
        cbUpper          = findViewById(R.id.cbUpper);
        cbLower          = findViewById(R.id.cbLower);
        cbDigits         = findViewById(R.id.cbDigits);
        cbSymbols        = findViewById(R.id.cbSymbols);
        progressStrength = findViewById(R.id.progressStrength);
        btnGenerate      = findViewById(R.id.btnGenerate);
        btnCopy          = findViewById(R.id.btnCopy);

        // Обработчики
        seekLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                tvLengthLabel.setText("Длина: " + value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnGenerate.setOnClickListener(this::onGenerateClicked);
        btnCopy.setOnClickListener(this::onCopyClicked);

        // Установим стартовую подпись длины
        tvLengthLabel.setText("Длина: " + seekLength.getProgress());
    }

    /* ========== КНОПКИ ========== */

    private void onGenerateClicked(View v) {
        int length = seekLength.getProgress();

        // Собираем выбранные наборы
        List<String> chosenSets = new ArrayList<>();
        if (cbUpper.isChecked())   chosenSets.add(UPPER);
        if (cbLower.isChecked())   chosenSets.add(LOWER);
        if (cbDigits.isChecked())  chosenSets.add(DIGITS);
        if (cbSymbols.isChecked()) chosenSets.add(SYMBOLS);

        if (chosenSets.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы один набор символов", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = buildPassword(length, chosenSets);
        etPassword.setText(password);
        updateStrengthMeter(password, chosenSets.size());
    }

    private void onCopyClicked(View v) {
        String text = etPassword.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(this, "Сначала сгенерируйте пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("password", text));
        Toast.makeText(this, "Пароль скопирован", Toast.LENGTH_SHORT).show();
    }

    /* ========== ГЕНЕРАЦИЯ ========== */

    /** Собирает пароль требуемой длины, гарантируя наличие хотя бы одного символа из каждого выбранного набора. */
    private String buildPassword(int length, List<String> sets) {
        List<Character> pwdChars = new ArrayList<>();

        // 1. Добавляем по одному символу из каждого набора, чтобы точно присутствовали
        for (String set : sets) {
            pwdChars.add(randomCharFrom(set));
        }

        // 2. Определяем объединённый пул
        StringBuilder all = new StringBuilder();
        for (String set : sets) all.append(set);

        // 3. Заполняем остальное
        while (pwdChars.size() < length) {
            pwdChars.add(randomCharFrom(all.toString()));
        }

        // 4. Перемешиваем
        Collections.shuffle(pwdChars, random);

        // 5. Переводим в строку
        StringBuilder result = new StringBuilder();
        for (char c : pwdChars) result.append(c);
        return result.toString();
    }

    private char randomCharFrom(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    /* ========== СТАТИСТИКА СИЛЫ ========== */

    /** Очень простая «оценка» силы: баллы за длину + за разнообразие наборов. */
    private void updateStrengthMeter(String password, int variety) {
        int len = password.length();
        int score = 0;

        // Длина
        if (len < 8)         score += 10;
        else if (len < 12)   score += 30;
        else if (len < 16)   score += 60;
        else                 score += 80;

        // Разнообразие наборов
        score += variety * 5;          // максимум ещё +20

        // Ограничиваем 0-100
        score = Math.min(score, 100);

        progressStrength.setProgress(score);

        String level;
        if (score < 30)       level = "Слабый";
        else if (score < 60)  level = "Средний";
        else if (score < 85)  level = "Хороший";
        else                  level = "Отличный";

        tvStrength.setText("Сила: " + level);
    }
}
