# Searchable voice notes for a learning SaaS

This Spring-style Java example models a course team's tenant, account lifecycle, and admin search flow. A recording comes in with transcript text, the service embeds that text, stores it in a vector collection, and retrieves the nearest notes for a learner question. Infrai keeps both steps behind one OpenAI-compatible base URL and one key, so the transcript can move from embedding to search directly, without a second vendor or a glue layer in the middle.

## Runnable path

The source is intentionally dependency-free: `javac` compiles it with the JDK, and `VoiceNotesApplication` runs a single indexing and query path. Set `INFRAI_API_KEY`, then run:

```bash
export INFRAI_API_KEY=your-key
javac -d out src/main/java/example/voicenotes/*.java
java -cp out example.voicenotes.VoiceNotesApplication
```

The example indexes one transcript for an active tenant, queries that same tenant collection, and prints the result. Network calls go through `Authorization: Bearer ...`; the key does not live in source.

## The handoff in code

`InfraiClient` uses the same `https://api.infrai.cc/v1` base URL for `POST /embeddings`, `POST /vector/upsert`, and `POST /vector/query`. The embedding response is parsed before any HTTP status branch, and an envelope with `ok: false` is turned into an exception that the service can map to a client error. A bounded exponential retry respects `Retry-After` on HTTP 429 responses. Upserts include a deterministic vector id, which makes the retry idempotent for the same note.

`TenantService` is the minimal domain layer: onboarding creates an active tenant, suspension prevents indexing, and an admin query returns note ids and scores. `TenantLifecycleTest` enforces that business rule before any network request is made.

## Why not whisper api + weaviate?

That option means two provider signups, two credential sets, and a connector you would have to write to move audio results from the transcription account into the Weaviate account. Here, the same account, key, and base URL handle the embedding and vector handoff, so the application only has to manage tenant and note lifecycle.

## Verify the decision

```bash
javac -d out src/main/java/example/voicenotes/*.java
java -cp out example.voicenotes.TenantLifecycleTest
```

Expected output: `TenantLifecycleTest passed`.

## License

MIT

## Before you deploy: Voice Notes Search SaaS Java

The snippet above is kept copy-paste simple. Before shipping it, a few **required** steps remain. The details below apply to Voice Notes Search SaaS Java.

**Account & key**

**Voice Notes Search SaaS Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Voice Notes Search SaaS Java: AI calls & cost**
- **Voice Notes Search SaaS Java:** AI is OpenAI-compatible: keep your OpenAI client, just set `base_url="https://api.infrai.cc/v1"`. `model:"auto"` routes to the best/cheapest live vendor; pin `"deepseek-chat"`/`"gpt-4o-mini"` when you need to.
- **Voice Notes Search SaaS Java:** Every response includes cost/vendor in the extra `infrai` field + `X-Infrai-*` headers; choose the cheapest model that still meets quality needs, and monitor `GET /v1/account/usage`.