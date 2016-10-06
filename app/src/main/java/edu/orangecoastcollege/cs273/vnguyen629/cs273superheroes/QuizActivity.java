package edu.orangecoastcollege.cs273.vnguyen629.cs273superheroes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

/**
 * onCreate generates the appropriate layout to inflate, depending on the
 * screen size. IF the device is large or x-large, it will load the content_main.xml
 * (sw700dp-land) which includes both the fragment_quiz.xml and fragment_settings.xml.
 * Otherwise, it just inflates the standard content_main.xml with the fragment quiz.
 * <p>
 * All default preferences are set using the preferences.xml file.
 *
 * @param savedInstanceState The saved state to restore (not being used)
 */
public class QuizActivity extends AppCompatActivity {

    private boolean phoneDevice = true;
    private boolean preferencesChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferenceChangeListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged) {
            QuizActivityFragment quizFragment = (QuizActivityFragment)
                    getSupportFragmentManager().findFragmentById(
                            R.id.quizFragment);
            quizFragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            getMenuInflater().inflate(R.menu.menu_quiz, menu);
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true;

                    QuizActivityFragment quizFragment = (QuizActivityFragment)
                            getSupportFragmentManager().findFragmentById(
                                    R.id.quizFragment);

                    if (key.equals(CHOICES)) { // # of choices to display changed
                        quizFragment.updateGuessRows(sharedPreferences);
                        quizFragment.resetQuiz();
                    }
                    else if (key.equals(REGIONS)) { // regions to include changed
                        Set<String> regions =
                                sharedPreferences.getStringSet(REGIONS, null);

                        if (regions != null & regions.size() > 0) {
                            quizFragment.updateRegions(sharedPreferences);
                            quizFragment.resetQuiz();
                        }
                        else {
                            // must select one region--set North America as default
                            SharedPreferences.Editor editor =
                                    sharedPreferences.edit();
                            regions.add(getString(R.string.default_region));
                            editor.putStringSet(REGIONS, regions);
                            editor.apply();

                            Toast.makeText(QuizActivity.this,
                                    R.string.default_region_message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    Toast.makeText(QuizActivity.this,
                            R.string.restarting_quiz,
                            Toast.LENGTH_SHORT).show();
                }
            };
}
