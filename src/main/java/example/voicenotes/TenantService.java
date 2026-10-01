package example.voicenotes;

final class TenantService {
  enum Status { ACTIVE, SUSPENDED }
  record Tenant(String id, String name, Status status) {}
  record VoiceNote(String id, String tenantId, String title, String transcript) {}

  private final InfraiClient client;
  TenantService(InfraiClient client) { this.client = client; }

  Tenant onboard(String id, String name) { return new Tenant(id, name, Status.ACTIVE); }

  String index(Tenant tenant, VoiceNote note) throws Exception {
    if (tenant.status() != Status.ACTIVE) throw new IllegalStateException("tenant is suspended");
    String embedding = client.post("/embeddings", "{\"model\":\"auto\",\"input\":" + quote(note.transcript()) + "}");
    String vector = "{\"collection\":\"tenant_" + tenant.id() + "\",\"vectors\":[{\"id\":\"" + note.id() + "\",\"embedding\":" + extractVector(embedding) + ",\"metadata\":{\"title\":" + quote(note.title()) + "}}]}";
    client.post("/vector/upsert", vector);
    return note.id();
  }

  String search(Tenant tenant, String question) throws Exception {
    if (tenant.status() != Status.ACTIVE) throw new IllegalStateException("tenant is suspended");
    String embedding = client.post("/embeddings", "{\"model\":\"auto\",\"input\":" + quote(question) + "}");
    return client.post("/vector/query", "{\"collection\":\"tenant_" + tenant.id() + "\",\"embedding\":" + extractVector(embedding) + ",\"top_k\":3,\"filter\":{},\"include_metadata\":true}");
  }

  private static String extractVector(String envelope) {
    int p = envelope.indexOf("\"embedding\"");
    if (p < 0) throw new IllegalStateException("embedding missing from response");
    int start = envelope.indexOf('[', p), end = envelope.indexOf(']', start);
    return envelope.substring(start, end + 1);
  }
  private static String quote(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
}
