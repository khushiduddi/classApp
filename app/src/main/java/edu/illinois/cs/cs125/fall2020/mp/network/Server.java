package edu.illinois.cs.cs125.fall2020.mp.network;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.models.Rating;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Development course API server.
 *
 * <p>Normally you would run this server on another machine, which the client would connect to over
 * the internet. For the sake of development, we're running the server right alongside the app on
 * the same device. However, all communication between the course API client and course API server
 * is still done using the HTTP protocol. Meaning that eventually it would be straightforward to
 * move this server to another machine where it could provide data for all course API clients.
 *
 * <p>You will need to add functionality to the server for MP1 and MP2.
 */
public final class Server extends Dispatcher {
  @SuppressWarnings({"unused", "RedundantSuppression"})
  private static final String TAG = Server.class.getSimpleName();

  private final Map<String, String> summaries = new HashMap<>();

  // summary/2020/fall
  private MockResponse getSummary(@NonNull final String path) {
    String[] parts = path.split("/");
    if (parts.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    String summary = summaries.get(parts[0] + "_" + parts[1]);
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(summary);
  }

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final Map<Summary, String> courses = new HashMap<>();

  // course/2020/fall/CS/125
  private MockResponse getCourse(@NonNull final String path) {
    String[] parts = path.split("/");
    final int x = 4;
    if (parts.length != x) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    Summary summary = new Summary(parts[0], parts[1], parts[2], parts[3]);
    String course = courses.get(summary);
    if (course == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(course);
  }

  private final Map<Summary, Map<String, Rating>> ratings = new HashMap<>();

  private MockResponse getRating(@NonNull final String path) throws JsonProcessingException {
    final int uuidLength = 36;
    final int x = 4;
    if (!(path.contains("?"))) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String[] pathParts = path.split("/");
    if (pathParts.length != x) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String[] numAndUuid = pathParts[3].split("\\?client=");
    Summary summary = new Summary();
    String u = pathParts[0] + "/" + pathParts[1] + "/" + pathParts[2] + "/" + numAndUuid[0];
    boolean flag = false;
    for (Summary sum : courses.keySet()) {
      if (u.equals(sum.getPath())) {
        flag = true;
        summary = sum;
      }
    }
    Map<String, Rating> inner = ratings.getOrDefault(summary, new HashMap<>());
    if (inner.get(numAndUuid[1]) == null) {
      inner.put(numAndUuid[1], new Rating(numAndUuid[1], Rating.NOT_RATED));
    }
    ratings.put(summary, inner);
    if (numAndUuid[1].length() != uuidLength) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    } else if (!flag) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    Rating rating = ratings.get(summary).get(numAndUuid[1]);
    return new MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(mapper.writeValueAsString(rating));
  }

  private boolean isJsonValid(final String text) {
    try {
      final ObjectMapper newMapper = new ObjectMapper();
      newMapper.readTree(text);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public MockResponse postRating(@NonNull final String path, @NonNull final RecordedRequest request)
    throws JsonProcessingException {
    String s = request.getBody().readUtf8();
    if (!(isJsonValid(s)) || (path.startsWith("/rating/")) || !(path.contains("?client"))) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    } else {
      Rating rating = mapper.readValue(s, Rating.class);
      System.out.println(rating.getRating());

      int pathX = path.indexOf("?");
      String urlPath = path.substring(0, pathX);
      Summary summ = new Summary();
      for (Summary sum : courses.keySet()) {
        if (sum.getPath().equals(urlPath)) {
          summ = sum;
        }
      }
      Map<String, Rating> m = new HashMap<>();
      if (ratings.get(summ) == null) {
        m.put(rating.getId(), rating);
        ratings.put(summ, m);
      } else {
        m = ratings.get(summ);
        m.put(rating.getId(), rating);
        ratings.put(summ, m);
      }
      System.out.println(summ.getNumber());
      return new MockResponse()
          .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
          .setHeader("Location", "");
    }
  }

  @NonNull
  @Override
  public MockResponse dispatch(@NonNull final RecordedRequest request) {
    try {
      String path = request.getPath();
      if (path == null || request.getMethod() == null) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
      } else if (path.equals("/") && request.getMethod().equalsIgnoreCase("HEAD")) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK);
      } else if (path.startsWith("/summary/")) {
        return getSummary(path.replaceFirst("/summary/", ""));
      } else if (path.startsWith("/course/")) {
        return getCourse(path.replaceFirst("/course/", ""));
      } else if (path.startsWith("/rating/") && request.getMethod().equals("GET")) {
        return getRating(path.replaceFirst("/rating/", ""));
      } else if (path.startsWith("/rating/") && request.getMethod().equals("POST")) {
        return postRating(path.replaceFirst("/rating/", ""), request);
      }
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    } catch (Exception e) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

  private static boolean started = false;

  /**
   * Start the server if has not already beennn started.
   *
   * <p>We start the server in a new thread so that it operates separately from and does not
   * interfere with the rest of the app.
   */
  public static void start() {
    if (!started) {
      new Thread(Server::new).start();
      started = true;
    }
  }

  private final ObjectMapper mapper = new ObjectMapper();

  private Server() {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    loadSummary("2020", "fall");
    loadCourses("2020", "fall");

    try {
      MockWebServer server = new MockWebServer();
      server.setDispatcher(this);
      server.start(CourseableApplication.SERVER_PORT);

      String baseUrl = server.url("").toString();
      if (!CourseableApplication.SERVER_URL.equals(baseUrl)) {
        throw new IllegalStateException("Bad server URL: " + baseUrl);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void loadSummary(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + "_summary.json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    summaries.put(year + "_" + semester, json);
  }

  @SuppressWarnings("SameParameterValue")
  private void loadCourses(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + ".json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    try {
      JsonNode nodes = mapper.readTree(json);
      for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
        JsonNode node = it.next();
        Summary course = mapper.readValue(node.toString(), Summary.class);
        courses.put(course, node.toPrettyString());
      }
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
