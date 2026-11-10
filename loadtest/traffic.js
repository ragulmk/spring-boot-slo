// Constant synthetic traffic (default 10 req/s) mixing reads and writes.
// Runs for 30 days; the container restarts it if it ever stops.
import http from 'k6/http';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const RPS = parseInt(__ENV.RPS || '10');

export const options = {
  scenarios: {
    steady: {
      executor: 'constant-arrival-rate',
      rate: RPS,
      timeUnit: '1s',
      duration: '720h',
      preAllocatedVUs: 20,
      maxVUs: 100,
    },
  },
  summaryTrendStats: ['avg', 'p(95)', 'p(99)'],
};

export default function () {
  const r = Math.random();
  if (r < 0.2) {
    http.post(`${BASE}/api/orders`,
      JSON.stringify({ item: 'item-' + Math.floor(Math.random() * 100), quantity: 1 + Math.floor(Math.random() * 5) }),
      { headers: { 'Content-Type': 'application/json' } });
  } else if (r < 0.6) {
    http.get(`${BASE}/api/orders/${1 + Math.floor(Math.random() * 50)}`);
  } else {
    http.get(`${BASE}/api/orders`);
  }
}
