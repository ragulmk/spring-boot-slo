# SLO Document: orders-service

| | |
|---|---|
| **Service** | orders-service (REST API for creating and reading orders) |
| **Owner** | Ragul M |
| **Window** | Rolling 30 days |
| **Status** | Active |

## Service level indicators (SLIs)

Measured at the service from Spring Boot's `http.server.requests` metric, for `/api/**` routes only. Health checks, metrics scraping and the `/chaos` admin endpoint are excluded because users never call them.

| SLI | Good event | Valid event |
|---|---|---|
| **Availability** | Response status is not 5xx | Any `/api/**` request |
| **Latency** | Response completed in ≤ 300ms | Any `/api/**` request |

4xx responses count as *good* for availability: a 404 for a missing order is the service working correctly.

## Service level objectives (SLOs)

| SLO | Objective | Error budget (30 days) |
|---|---|---|
| Availability | **99.5%** of requests succeed | 0.5% of requests ≈ 3.6 hours of full outage |
| Latency | **99%** of requests under 300ms | 1% of requests may be slower |

## Alerting

Alerts fire on **burn rate** (how fast the error budget is being spent), not on raw error counts. Each alert requires both a long and a short window to exceed the threshold.

| Alert | Windows | Burn rate | Budget spent | Severity |
|---|---|---|---|---|
| BurnRateFast | 1h + 5m | 14.4x | 2% in 1 hour | critical (page) |
| BurnRateMedium | 6h + 30m | 6x | 5% in 6 hours | critical (page) |
| BurnRateSlow | 1d + 2h | 3x | 10% in 1 day | warning (ticket) |
| BurnRateSlower | 3d + 6h | 1x | 10% in 3 days | warning (ticket) |

## Error budget policy

| Budget remaining | Action |
|---|---|
| **> 50%** | Normal: ship features as usual. |
| **25-50%** | Caution: review recent incidents; risky changes need a rollback plan. |
| **< 25%** | Slow down: prioritise reliability work in the next sprint. |
| **Exhausted (≤ 0%)** | Freeze: only bug fixes and reliability improvements until the budget recovers. |

A single incident that consumes more than 20% of the budget requires a written postmortem.

## Review

Review these SLOs every quarter. If users are unhappy while the SLO is met, tighten it; if the team is constantly firefighting with no user complaints, loosen it.
