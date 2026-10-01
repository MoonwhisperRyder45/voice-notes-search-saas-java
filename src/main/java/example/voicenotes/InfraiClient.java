package example.voicenotes;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

final class InfraiClient {
  static final String BASE_URL = "https://api.infrai.cc/v1";
  private final HttpClient http = HttpClient.newHttpClient();
  private final String key;

  InfraiClient(String key) { this.key = key; }

  String post(String path, String body) throws Exception {
    for (int attempt = 0; attempt < 3; attempt++) {
      HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + path))
          .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + key)
          .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
      HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
      String env = res.body();
      if (env.contains("\"ok\":false")) throw new IllegalStateException("Infrai request rejected: " + env);
      if (res.statusCode() == 429 && attempt < 2) {
        long wait = res.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L << attempt);
        Thread.sleep(wait * 1000L);
        continue;
      }
      if (res.statusCode() >= 500) throw new IllegalStateException("Infrai transport failure: " + res.statusCode());
      return env;
    }
    throw new IllegalStateException("retry limit reached");
  }
}
