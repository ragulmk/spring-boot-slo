#!/usr/bin/env bash
# Control fault injection on the running orders-service.
#   ./scripts/chaos.sh errors 0.2     -> 20% of /api requests return 503
#   ./scripts/chaos.sh latency 500    -> add 500ms to every /api request
#   ./scripts/chaos.sh off            -> back to normal
#   ./scripts/chaos.sh status
set -euo pipefail
URL="${APP_URL:-http://localhost:8080}/chaos"

case "${1:-status}" in
  errors)  curl -sf -X POST "$URL?errorRate=${2:-0.2}" ;;
  latency) curl -sf -X POST "$URL?latencyMs=${2:-500}" ;;
  off)     curl -sf -X DELETE "$URL" ;;
  status)  curl -sf "$URL" ;;
  *) echo "usage: $0 {errors <0-1>|latency <ms>|off|status}" >&2; exit 1 ;;
esac
echo
