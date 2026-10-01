package example.voicenotes;

public final class VoiceNotesApplication {
  public static void main(String[] args) throws Exception {
    String key = System.getenv("INFRAI_API_KEY");
    if (key == null || key.isBlank()) throw new IllegalArgumentException("Set INFRAI_API_KEY first");
    TenantService service = new TenantService(new InfraiClient(key));
    TenantService.Tenant tenant = service.onboard("acme-learning", "Acme Learning");
    TenantService.VoiceNote note = new TenantService.VoiceNote("note-101", tenant.id(), "Lesson planning", "We will review fractions in the next course session.");
    service.index(tenant, note);
    String matches = service.search(tenant, "What is the next fractions lesson?");
    System.out.println("Indexed " + note.title() + " for " + tenant.name());
    System.out.println("Search response: " + matches);
  }
}
