#!/usr/bin/env bash
set -euo pipefail

# Reference script (not required when using docker-compose service 'kafka-init').
# Topics used in this project (see libs:event-contract Topics.java):
#   rfid.raw
#   rfid.normalized
#   mes.domain.events
#   integration.erp.inbound
#   integration.erp.outbox
#   dlq
#   mes.logs
#   site.telemetry.raw
#   production.result

echo "Use docker-compose 'kafka-init' service to auto-create topics."
