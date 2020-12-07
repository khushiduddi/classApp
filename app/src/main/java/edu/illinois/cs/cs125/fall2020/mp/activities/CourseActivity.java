package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
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
import edu.illinois.cs.cs125.fall2020.mp.models.Rating;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;
import edu.illinois.cs.cs125.fall2020.mp.

/**
 * Create new class Course Activity.
 *
 */
public class CourseActivity extends AppCompatActivity
    implements Client.CourseClientCallbacks, RatingBar.OnRatingBarChangeListener {
  private static final String TAG = CourseActivity.class.getSimpleName();
  private ActivityCourseBinding binding;
  private Summary sum = new Summary();
  private final ObjectMapper mapper = new ObjectMapper();

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
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    String description = intent.getStringExtra("COURSE");
    binding =  DataBindingUtil.setContentView(this, R.layout.activity_course);
    try {
      sum = mapper.readValue(description, Summary.class);
      Course course = mapper.readValue(description, Course.class);
      String temp = String.format(
          "%s %s:  %s\n", sum.getDepartment(), sum.getNumber(), sum.getTitle());
      binding.title1.setText(temp);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    CourseableApplication application = (CourseableApplication) getApplication();
    application.getCourseClient().getCourse(sum, this);
    application.getCourseClient().getRating(sum, application.getClientID(), this);
    binding.rating.setOnRatingBarChangeListener(this);
    // Bind to the layout in activity_main.xml
    /*
    binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
    binding.title1.setText(title);
    binding.description.setText(newDescription);
     */
  }

  /**
   * Method gets description and title of a course.
   *
   * @param summary summary was retrieved
   * @param course course was retrieved
   */
  @Override
  public void courseResponse(
      final Summary summary, final Course course) {
    binding.description.setText(course.getDescription());
  }

  public void yourRating(final Summary sam, final Rating r) {
    this.sum = sam;
    binding.rating.setRating((float) r.getRating());
  }

  /**
      * This changes the rating as you click on it.
      *
      * @param ratingBar is the specified rating bar for the course
      * @param rating is the rating for the course
      * @param fromUser is the input from the user
      */
  @Override
  public void onRatingChanged(
      final RatingBar ratingBar, final float rating, final boolean fromUser) {
  CourseableApplication app = (CourseableApplication) getApplication();
  Rating r = new Rating(app.getClientID(), rating);
  app.getCourseClient().postRating(sum, r, this);
  }
}
}

