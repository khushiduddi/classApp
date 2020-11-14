package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
//import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


import edu.illinois.cs.cs125.fall2020.mp.R;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.fall2020.mp.models.Course;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;

/**
 * Create new class Course Activity.
 *
 */
public class CourseActivity extends AppCompatActivity implements Client.CourseClientCallbacks {
  private static final String TAG = CourseActivity.class.getSimpleName();
  private ActivityCourseBinding binding;
  private String newDescription;
  private String title;

  /**
   * Creates new action for CourseActivity.
   *
   * @param savedInstanceState retrieves savedInstanceState
   */
  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    Log.i(TAG, "Course Activity Started");
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    String description = intent.getStringExtra("COURSE");
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      Summary courses = mapper.readValue(description, Summary.class);
      CourseableApplication application = (CourseableApplication) getApplication();
      application.getCourseClient().getCourse(courses, this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    // Bind to the layout in activity_main.xml
    binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
    binding.title1.setText(title);
    binding.description.setText(newDescription);
  }

  /**
   * Gets description and title of a course.
   *
   * @param summary summary was retrieved
   * @param course course was retrieved
   */
  @Override
  public void courseResponse(
      final Summary summary, final Course course) {
    newDescription = course.getDescription();
    title = course.getTitle();
  }
}
