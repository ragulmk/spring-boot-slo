# SLOLatencyBurnRate (Fast / Medium / Slow / Slower)

**SLO:** 99% of `/api/**` requests complete within 300ms (30-day window).

## What this alert means
Too many requests are slower than 300ms. Users experience a sluggish service even though requests succeed.

## Diagnose
1. Open **Orders Service - SLO Overview**: compare the *Latency percentiles* panel with the 300ms line.
2. Find the slow endpoints with the *Latency by endpoint (p99)* panel.
3. Check JVM pressure: GC pause time spikes or heap near max often cause latency.
4. Check host resources (CPU throttling, memory limit): `docker stats orders-service`.
5. Check whether traffic increased: *Requests per second by status*.
6. Check if latency chaos is on (demo environment): `curl localhost:8080/chaos`.

## Fix
- Roll back a release that introduced slow code or queries.
- Scale up (more CPU/memory) or out (more instances) if load increased.
- Tune JVM memory if GC is the cause (increase heap, check for leaks).
- Disable chaos in the demo: `make chaos-off`.

## Afterwards
- Record the budget consumed and the root cause.
- Consider a load test before the next release to catch regressions earlier.
