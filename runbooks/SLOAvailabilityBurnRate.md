# SLOAvailabilityBurnRate (Fast / Medium / Slow / Slower)

**SLO:** 99.5% of `/api/**` requests do not return 5xx (30-day window).

## What this alert means
The service is returning server errors fast enough to threaten the monthly error budget.
- **Fast / Medium (critical):** a real incident is happening now. Act immediately.
- **Slow / Slower (warning):** a low-level error rate is steadily eating the budget. Fix within working hours.

## Diagnose
1. Open **Orders Service - SLO Overview** in Grafana. Check *Requests per second by status* and *Latency by endpoint*.
2. Find which endpoints fail: in Prometheus run
   `sum by (uri, status) (rate(http_server_requests_seconds_count{job="orders-service", status=~"5.."}[5m]))`
3. Check the application logs: `docker compose logs --tail=200 orders-service`.
4. Check if chaos injection is on (demo environment): `curl localhost:8080/chaos`.
5. Check for a recent deploy, config change or dependency (database, external API) failure.
6. Check JVM health on the dashboard: heap near max, long GC pauses, thread count spikes.

## Fix
- Roll back the most recent deploy if errors started right after it.
- Restart the service if it's in a bad state (e.g. memory exhausted): `docker compose restart orders-service`.
- Disable chaos in the demo: `make chaos-off`.
- If a dependency is down, fail gracefully (circuit breaker, cached responses) and escalate to its owner.

## Afterwards
- Note how much error budget the incident used (*Error budget remaining* panel).
- If it used more than 20% of the budget, write a postmortem (see the error budget policy in `docs/SLO.md`).
