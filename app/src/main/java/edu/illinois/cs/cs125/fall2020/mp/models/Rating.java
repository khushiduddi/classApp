package edu.illinois.cs.cs125.fall2020.mp.models;

/** Rating class for storing client ratings of courses. */
public class Rating {
  /** Rating indicating that the course has not been rated yet. */
  public static final double NOT_RATED = -1.0;

  private String id;
  private double rating;

  /** Default constructor for Rating. */
  public Rating() {}

  /**
   * Constructor for Rating.
   *
   * @param setId will set ID.
   * @param setRating will set rating.
   */
  public Rating(final String setId, final double setRating) {
    id = setId;
    rating = setRating;
  }

  /**
   * Retrieves Id.
   *
   * @return id will retrieve ID.
   */
  public String getId() {
    return id;
  }

  /**
   * Retrieve rating.
   *
   * @return rating will retrieve rating.
   */
  public double getRating() {
    return rating;
  }
}
