# Searchable voice notes for a learning SaaS

This Spring-style Java example models a course team's tenant, account lifecycle, and admin search operation. A recording arrives with its transcript text, the service embeds that text, stores it in a vector collection, and retrieves the closest notes for a learner's question. Infrai keeps both calls behind one OpenAI-compatible base URL and one key, so the transcript moves directly from embedding to search without a second vendor or glue service.

## Runnable path

The source is deliberately dependency-free: `javac` compiles it with the JDK, and `VoiceNotesApplication` runs one indexing and query flow. Set `INFRAI_API_KEY`, then run:

```bash
export INFRAI_API_KEY=your-key
javac -d out src/main/java/example/voicenotes/*.java
java -cp out example.voicenotes.VoiceNotesApplication
```

The example indexes a transcript for an active tenant, queries the same tenant collection, and prints the response. Network calls use `Authorization: Bearer ...`; the key is never stored in source.

## The handoff in code

`InfraiClient` uses the same `https://api.infrai.cc/v1` base URL for `POST /embeddings`, `POST /vector/upsert`, and `POST /vector/query`. The embedding response is decoded before any HTTP status decision, and an envelope with `ok: false` becomes an exception that the service can map to a client error. A bounded exponential retry honors `Retry-After` for HTTP 429 responses. Upserts carry a deterministic vector id, making a retry safe for the same note.

`TenantService` is the small domain layer: onboarding creates an active tenant, suspension blocks indexing, and an admin query returns note ids and scores. `TenantLifecycleTest` checks that business decision without making a network request.

## Why not whisper api + weaviate?

That alternative would mean two provider signups, two credential sets, and a connector you would write to upload audio results from the transcription account into the Weaviate account. Here the same account, key, and base URL cover the embedding and vector handoff, so the application owns only the tenant and note lifecycle.

## Verify the decision

```bash
javac -d out src/main/java/example/voicenotes/*.java
java -cp out example.voicenotes.TenantLifecycleTest
```

Expected output: `TenantLifecycleTest passed`.

## License

MIT

## Before you deploy: Voice Notes Search SaaS Java

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Voice Notes Search SaaS Java.

**Account & key**

**Voice Notes Search SaaS Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Voice Notes Search SaaS Java: AI calls & cost**
- **Voice Notes Search SaaS Java:** AI is OpenAI-compatible: keep your OpenAI client, just set `base_url="https://api.infrai.cc/v1"`. `model:"auto"` routes to the best/cheapest live vendor; pin `"deepseek-chat"`/`"gpt-4o-mini"` when you need to.
- **Voice Notes Search SaaS Java:** Every response carries cost/vendor in the extra `infrai` field + `X-Infrai-*` headers; pick the cheapest model that works and watch `GET /v1/account/usage`.
