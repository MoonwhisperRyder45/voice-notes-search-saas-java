package example.voicenotes;

public final class TenantLifecycleTest {
  public static void main(String[] args) {
    TenantService service = new TenantService(new InfraiClient("test"));
    TenantService.Tenant tenant = service.onboard("school-1", "Course House");
    if (tenant.status() != TenantService.Status.ACTIVE) throw new AssertionError("new tenant must be active");
    TenantService.Tenant suspended = new TenantService.Tenant(tenant.id(), tenant.name(), TenantService.Status.SUSPENDED);
    try {
      service.index(suspended, new TenantService.VoiceNote("n", suspended.id(), "x", "text"));
      throw new AssertionError("suspended tenant was allowed to index");
    } catch (IllegalStateException expected) {
      if (!expected.getMessage().contains("suspended")) throw expected;
    } catch (Exception unexpected) { throw new AssertionError(unexpected); }
    System.out.println("TenantLifecycleTest passed");
  }
}
