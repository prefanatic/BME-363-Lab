/*
 * Copyright 2015 Cody Goldberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uri.egr.bme363lab.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import edu.uri.egr.bme363lab.R;
import timber.log.Timber;

public class HealthGuessActivity extends AppCompatActivity {
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.gender) RadioGroup genderGroup;
    @Bind(R.id.fitness) Spinner fitnessSpinner;
    @Bind(R.id.heart_health_title) TextView heartTitle;
    @Bind(R.id.heart_health) TextView heartGuess;
    @Bind(R.id.bmi_title) TextView bmiTitle;
    @Bind(R.id.bmi) TextView bmiGuess;
    @Bind(R.id.weight_input_layout) TextInputLayout weightInputLayout;
    @Bind(R.id.height_input_layout) TextInputLayout heightInputLayout;
    @Bind(R.id.age_input_layout) TextInputLayout ageInputLayout;
    @Bind(R.id.weight) EditText weightEntry;
    @Bind(R.id.height) EditText heightEntry;
    @Bind(R.id.age) EditText ageEntry;
    @Bind(R.id.calculate) Button calculateButton;

    /*
        3D array to hold the heart rate ranges.
        Constructed with two genders, six categories for "age", and seven categories for "fitness"
     */
    private String[][][] healthArray = new String[][][]{
            // Male Construction
            {
                    {"49-55", "49-54", "50-56", "50-57", "51-56", "50-55"}, // Athlete
                    {"56-61", "55-61", "57-62", "58-63", "57-61", "56-61"}, // Excellent
                    {"62-65", "62-65", "63-66", "64-67", "62-67", "62-65"}, // Good
                    {"66-69", "66-70", "67-70", "68-71", "68-71", "66-69"}, // Above Average
                    {"70-73", "71-74", "71-75", "72-76", "72-75", "70-73"}, // Average
                    {"74-81", "75-81", "76-82", "77-83", "76-81", "74-79"}, // Below Average
                    {"82+", "82+", "83+", "84+", "82+", "80+"}              // Poor
            },

            // Female Construction
            {
                    {"54-60", "54-59", "54-59", "54-60", "54-59", "54-59"}, // Athlete
                    {"61-65", "60-64", "60-64", "61-65", "60-64", "60-64"}, // Excellent
                    {"66-69", "65-68", "65-69", "66-69", "65-68", "65-68"}, // Good
                    {"70-73", "69-72", "70-73", "70-73", "69-73", "69-72"}, // Above Average
                    {"74-78", "73-76", "74-78", "74-77", "74-77", "73-76"}, // Average
                    {"79-84", "77-82", "79-84", "78-83", "78-83", "77-84"}, // Below Average
                    {"85+", "83+", "85+", "84+", "84+", "84+"}              // Poor
            }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Same deal as in the MainActivity - set our layout we've made to inflate, and inject ButterKnife.
        setContentView(R.layout.activity_health_guess);
        ButterKnife.bind(this);

        // We also need to set our toolbar as a SupportActionBar, and then enable the back button for it.
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Check to see what the ID of this item is.
        switch(item.getItemId()) {

            // If this item is our back button, we should now close and return to the MainActivity.
            case android.R.id.home:
                finish();

                return true;
        }

        return false;
    }

    /*
        Assign listeners to our weight, height, and age entries.
        All three of these will pipe into the "validateInputLayout" and control the errors.
        Since all three entries do the same thing, we can reuse code here and pass the different inputLayout instead.
     */

    @OnTextChanged(R.id.weight)
    public void validateWeightEntry(CharSequence text) {
        validateInputLayout(text.toString(), weightInputLayout);
    }

    @OnTextChanged(R.id.height)
    public void validateHeightEntry(CharSequence text) {
        validateInputLayout(text.toString(), heightInputLayout);
    }

    @OnTextChanged(R.id.age)
    public void validateAgeEntry(CharSequence text) {
        validateInputLayout(text.toString(), ageInputLayout);
    }

    private void validateInputLayout(String text, TextInputLayout layout) {
        if (!text.isEmpty()) {
            int integerValidation = Integer.valueOf(text);

            if (integerValidation == 0) {
                layout.setErrorEnabled(true);
                layout.setError("Cannot be 0");
            } else {
                layout.setErrorEnabled(false);
            }
        } else {
            layout.setErrorEnabled(true);
            layout.setError("Cannot be empty");
        }
    }

    @SuppressWarnings("ConstantConditions")
    @OnClick(R.id.calculate)
    public void calculateBmiAndAge() {
        // Lower our keyboard so we can see our results!
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View decorView = getWindow().getDecorView();

        decorView.clearFocus();
        inputMethodManager.hideSoftInputFromWindow(decorView.getWindowToken(), 0);

        String weight = weightEntry.getText().toString();
        String height = heightEntry.getText().toString();
        String age = ageEntry.getText().toString();
        int fitness = fitnessSpinner.getSelectedItemPosition();

        View selectedGenderView = genderGroup.findViewById(genderGroup.getCheckedRadioButtonId());
        int gender = genderGroup.indexOfChild(selectedGenderView);

        Timber.d("Weight (%s) - Height (%s) - Age (%s) - Gender (%d) - Fitness (%d)", weight, height, age, gender, fitness);

        if (weight.isEmpty() || height.isEmpty() || age.isEmpty() || gender == -1) {
            Snackbar.make(mToolbar, R.string.health_cannot_calculate, Snackbar.LENGTH_LONG).show();
            return;
        }

        int weightInt = Integer.valueOf(weight);
        int heightInt = Integer.valueOf(height);
        int ageInt = Integer.valueOf(age);
        bmiGuess.setText(String.format("%.2f", calculateBmi(weightInt, heightInt)));
        heartGuess.setText(healthArray[gender][fitness][collapseAge(ageInt)]);
    }

    /**
     * collapseAge
     * Takes the age given and collapses it down to the correct index in our 3D healthArray table.
     * @param age Integer of age.
     * @return Index in healthArray.
     */
    private int collapseAge(int age) {
        if (age >= 65)
            return 5;
        if (age >= 56)
            return 4;
        if (age >= 46)
            return 3;
        if (age >= 36)
            return 2;
        if (age >= 26)
            return 1;
        return 0;
    }

    /**
     * calculateBmi
     * It calculates your BMI!
     * @param weight Integer of weight (pounds)
     * @param height Integer of height (inches)
     * @return Double of BMI
     */
    private double calculateBmi(int weight, int height) {
        return (weight * 703) / (Math.pow(height, 2));
    }
}
