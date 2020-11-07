package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
//import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Create new class Course Activity.
 *
 */
public class CourseActivity extends AppCompatActivity {
    private static final String TAG = CourseActivity.class.getSimpleName();

    /**
     * Creates new action for CourseActivity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "Course Activity Started");
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Log.d(TAG, intent.getStringExtra("TITLE"));
    }
}
