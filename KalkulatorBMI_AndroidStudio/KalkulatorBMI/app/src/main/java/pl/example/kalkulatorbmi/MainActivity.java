package pl.example.kalkulatorbmi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private EditText etHeight;
    private EditText etWeight;
    private RadioGroup rgGender;
    private CheckBox cbSave;
    private TextView tvResult;
    private SharedPreferences preferences;

    private static final String PREFS_NAME = "bmi_preferences";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_SAVE = "save_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        rgGender = findViewById(R.id.rgGender);
        cbSave = findViewById(R.id.cbSave);
        tvResult = findViewById(R.id.tvResult);
        Button btnCalculate = findViewById(R.id.btnCalculate);
        Button btnClear = findViewById(R.id.btnClear);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSavedData();

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateBmi();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearForm();
            }
        });

        rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Listener pokazuje reakcję aplikacji na zmianę wybranej opcji.
                if (checkedId != -1) {
                    Toast.makeText(MainActivity.this, "Zmieniono wybór", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cbSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                preferences.edit().clear().apply();
                Toast.makeText(MainActivity.this, "Zapisane dane usunięte", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateBmi() {
        String heightText = etHeight.getText().toString().trim().replace(',', '.');
        String weightText = etWeight.getText().toString().trim().replace(',', '.');

        etHeight.setError(null);
        etWeight.setError(null);

        if (TextUtils.isEmpty(heightText)) {
            etHeight.setError("Podaj wzrost w cm");
            etHeight.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(weightText)) {
            etWeight.setError("Podaj wagę w kg");
            etWeight.requestFocus();
            return;
        }

        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Wybierz płeć w formularzu", Toast.LENGTH_SHORT).show();
            return;
        }

        final double heightCm;
        final double weightKg;
        try {
            heightCm = Double.parseDouble(heightText);
            weightKg = Double.parseDouble(weightText);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Wpisz poprawne wartości liczbowe", Toast.LENGTH_SHORT).show();
            return;
        }

        if (heightCm <= 0 || heightCm > 300) {
            etHeight.setError("Wzrost musi być większy od 0 i nie większy niż 300 cm");
            etHeight.requestFocus();
            return;
        }

        if (weightKg <= 0 || weightKg > 500) {
            etWeight.setError("Waga musi być większa od 0 i nie większa niż 500 kg");
            etWeight.requestFocus();
            return;
        }

        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);
        String category = getBmiCategory(bmi);
        String result = String.format(Locale.getDefault(),
                "Twoje BMI: %.1f\nInterpretacja: %s", bmi, category);
        tvResult.setText(result);

        if (cbSave.isChecked()) {
            preferences.edit()
                    .putString(KEY_HEIGHT, heightText)
                    .putString(KEY_WEIGHT, weightText)
                    .putInt(KEY_GENDER, rgGender.getCheckedRadioButtonId())
                    .putBoolean(KEY_SAVE, true)
                    .apply();
            Toast.makeText(this, "Dane zapisane na tym urządzeniu", Toast.LENGTH_SHORT).show();
        } else {
            preferences.edit().clear().apply();
        }
    }

    private String getBmiCategory(double bmi) {
        if (bmi < 18.5) {
            return "Niedowaga";
        } else if (bmi < 25.0) {
            return "Waga prawidłowa";
        } else if (bmi < 30.0) {
            return "Nadwaga";
        } else {
            return "Otyłość";
        }
    }

    private void loadSavedData() {
        boolean shouldLoad = preferences.getBoolean(KEY_SAVE, false);
        cbSave.setChecked(shouldLoad);
        if (shouldLoad) {
            etHeight.setText(preferences.getString(KEY_HEIGHT, ""));
            etWeight.setText(preferences.getString(KEY_WEIGHT, ""));
            int genderId = preferences.getInt(KEY_GENDER, -1);
            if (genderId != -1) {
                rgGender.check(genderId);
            }
        }
    }

    private void clearForm() {
        etHeight.setText("");
        etWeight.setText("");
        rgGender.clearCheck();
        cbSave.setChecked(false);
        tvResult.setText("Wynik BMI pojawi się tutaj.");
        etHeight.setError(null);
        etWeight.setError(null);
        preferences.edit().clear().apply();
        Toast.makeText(this, "Formularz wyczyszczony", Toast.LENGTH_SHORT).show();
    }
}
