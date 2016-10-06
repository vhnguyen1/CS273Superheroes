package edu.orangecoastcollege.cs273.vnguyen629.cs273superheroes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * A placeholder fragment containing a simple view.
 */
public class QuizActivityFragment extends Fragment {

    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;

    private TextView questionNumberTextView;
    private ImageView superheroImageView;
    private LinearLayout[] guessLinearLayout;
    private TextView answerTextView;

    private List<String> fileNameList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =
                inflater.inflate(R.layout.fragment_quiz, container, false);

        random = new SecureRandom();
        handler = new Handler();

        questionNumberTextView =
                (TextView) view.findViewById(R.id.questionNumberTextView);

        superheroImageView = (ImageView) view.findViewById(R.id.superheroImageView);

        guessLinearLayout = new LinearLayout[4];
        guessLinearLayout[0] =
                (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayout[1] =
                (LinearLayout) view.findViewById(R.id.row2LinearLayout);

        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        return view;
    }

    /**
     * Utility method that disables all answer Buttons
     */
    public void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayout[row];
            for (int i = 0; i < guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }

    public void resetQuiz() {
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try {
            // loop through each region
            for (String region : regionSet) {
                // get a list of all flag image files in this region
                String[] paths = assets.list(region);

                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0;
        totalGuesses = 0;

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        // add FLAGS_IN_QUIZ random file names to the quizCountriesList
        while (flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);

            // get the random file name
            String fileName = fileNameList.get(randomIndex);

            // if the region is enabled and it hasn't already been chosen
            if (!quizCountriesList.contains(fileName)) {
                quizCountriesList.add(fileName); // add the file to the list
                flagCounter++;
            }
        }

        loadNextFlag(); // start the quiz by loading the first flag
    }

    /**
     * After user guesses a flag correctly, load the next flag.
     */
    private void loadNextFlag() {
        // get file name of the next flag and remove it from the list
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage; // update the correct answer
        answerTextView.setText(""); // clear answerTextView

        // display current question number
        questionNumberTextView.setText(getString(
                R.string.question, (correctAnswers + 1), FLAGS_IN_QUIZ));

        // extract the region from the next image's name
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        // use AssetManager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        // get an InputStream to the asset representing the next flag
        // and try to use the inputStream
        try (InputStream stream =
                     assets.open(region + "/" + nextImage + ".png")) {
            // load the asset as a Drawable and display on the flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading " + nextImage, exception);
        }

        Collections.shuffle(fileNameList); // shuffle file names

        // put the correct answer at the end of fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // add 2, 4, or 8 guess Buttons based on the value of guessRows
        for (int row = 0; row < guessRows; row++) {
            // place Buttons in currentTableRow
            for (int column = 0;
                 column < guessLinearLayout[row].getChildCount();
                 column++) {
                // get reference to Button to configure
                Button newGuessButton =
                        (Button) guessLinearLayout[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // get country name and set it as newGuessButton's text
                String fileName = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(fileName));
            }
        }

        // randomly replace one Button with the correct answer
        int row = random.nextInt(guessRows); // pick random row
        int column = random.nextInt(2); // pick random column
        LinearLayout randomRow = guessLinearLayout[row]; // get the row
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    /**
     * Called when a guess button is clicked. This listener is used for all buttons
     * in the flag quiz.
     */
    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            totalGuesses++; // increments the number of guesses the user has made

            if (guess.equals(answer)) { // if the guess is correct
                correctAnswers++; // increment the number of correct answers

                // display correct answer in green text
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correct_answer,
                                getContext().getTheme()));

                disableButtons();

                // if the user has correctly identified FLAGS_IN_QUIZ flags
                if (correctAnswers == FLAGS_IN_QUIZ) {
                    // DialogFragment to display quiz state and start new quiz
                    DialogFragment quizResults =
                            new DialogFragment() {
                                // create an AlertDialog and return it
                                @Override
                                public Dialog onCreateDialog(Bundle bundle) {
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(getActivity());
                                    builder.setMessage(
                                            getString(R.string.results,
                                                    totalGuesses,
                                                    (1000 / (double) totalGuesses)));

                                    // "Reset Quiz" Button
                                    builder.setPositiveButton(R.string.reset_quiz,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    resetQuiz();
                                                }
                                            });

                                    return builder.create(); // return the AlertDialog
                                }
                            };

                    // use FragmentManager to display the DialogFragment
                    quizResults.setCancelable(false);
                    quizResults.show(getFragmentManager(), "quiz results");
                }
                else { // answer is correct but quiz is not over
                    // load the next flag after a 2 second delay
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    loadNextFlag();
                                }
                            }, 2000); // 2000 millisecons for 2 second delay
                }
            }
            else { // answer was incorrect
                // display "Incorrect!" in red
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(
                        R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false); // disable incorrect answer
            }
        }
    };
}
