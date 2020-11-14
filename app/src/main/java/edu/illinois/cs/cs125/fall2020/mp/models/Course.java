package edu.illinois.cs.cs125.fall2020.mp.models;

/** Gets a description for the course. */
public class Course extends Summary {
  private String description;

  /** Constructor for course. */
  public Course() {}

  /**
   * Gets the description.
   *
   * @return description
   */
  public String getDescription() {
    return description;
  }
}
