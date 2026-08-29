#!/usr/bin/env bash
# Starts, stops, and reports on the whole microservices stack for local dev.
# Usage: ./services.sh start|stop|status [--build]

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

LOG_DIR="logs"
mkdir -p "$LOG_DIR"

# name -> "dir:port"
SERVICES=(
    "eureka-server:eureka-server:8761"
    "config-server:config-server:8888"
    "notification-service:notification-service:8081"
    "activity-log-service:activity-log-service:8082"
    "follow-service:follow-service:8083"
    "user-service:user-service:8084"
    "content-service:content-service:8085"
    "gateway:gateway:8090"
)

INFRA_DBS=(notification-service activity-log-service follow-service user-service content-service)

jar_for() {
    local dir="$1"
    find "$dir/target" -maxdepth 1 -name "*.jar" ! -name "*.original" 2>/dev/null | head -1
}

build_if_needed() {
    local dir="$1"
    local force="$2"
    if [[ -n "$force" || -z "$(jar_for "$dir")" ]]; then
        echo "Building $dir..."
        (cd "$dir" && mvn -q clean package -DskipTests)
    fi
}

wait_for_port() {
    local port="$1"
    local name="$2"
    for _ in $(seq 1 60); do
        if curl -s -o /dev/null "http://localhost:$port/"; then
            echo "$name is up on $port"
            return 0
        fi
        sleep 1
    done
    echo "WARNING: $name did not respond on $port within 60s (continuing anyway)"
}

start_service() {
    local name="$1" dir="$2" port="$3"
    if lsof -ti "tcp:$port" -sTCP:LISTEN >/dev/null 2>&1; then
        echo "$name already running on $port, skipping"
        return
    fi
    local jar jar_rel
    jar="$(jar_for "$dir")"
    jar_rel="${jar#"$dir"/}"
    echo "Starting $name (port $port)..."
    (cd "$dir" && nohup java -jar "$jar_rel" > "../$LOG_DIR/$name.log" 2>&1 &)
}

cmd_start() {
    local force_build=""
    [[ "${1:-}" == "--build" ]] && force_build="1"

    echo "== Starting shared infra (RabbitMQ) =="
    docker compose -f infra-compose.yml up -d

    echo "== Starting per-service databases =="
    for svc in "${INFRA_DBS[@]}"; do
        (cd "$svc" && docker compose up -d)
    done

    echo "== Building jars where needed =="
    for entry in "${SERVICES[@]}"; do
        IFS=":" read -r name dir _ <<< "$entry"
        build_if_needed "$dir" "$force_build"
    done

    echo "== Starting Eureka =="
    start_service "eureka-server" "eureka-server" 8761
    wait_for_port 8761 "eureka-server"

    echo "== Starting Config Server =="
    start_service "config-server" "config-server" 8888
    wait_for_port 8888 "config-server"

    echo "== Starting domain services =="
    for entry in "${SERVICES[@]}"; do
        IFS=":" read -r name dir port <<< "$entry"
        [[ "$name" == "eureka-server" || "$name" == "config-server" || "$name" == "gateway" ]] && continue
        start_service "$name" "$dir" "$port"
    done
    for entry in "${SERVICES[@]}"; do
        IFS=":" read -r name dir port <<< "$entry"
        [[ "$name" == "eureka-server" || "$name" == "config-server" || "$name" == "gateway" ]] && continue
        wait_for_port "$port" "$name"
    done

    echo "== Starting Gateway =="
    start_service "gateway" "gateway" 8090
    wait_for_port 8090 "gateway"

    echo
    echo "All services started. Logs are in $LOG_DIR/. Gateway: http://localhost:8090"
    echo "Eureka dashboard: http://localhost:8761"
    echo "Note: the gateway may return 503s for the first ~30s - Eureka clients only"
    echo "refresh their registry view every 30s by default, so newly-started"
    echo "services take a moment to become routable even though they're already up."
}

cmd_stop() {
    echo "== Stopping application processes =="
    for entry in "${SERVICES[@]}"; do
        IFS=":" read -r name _ port <<< "$entry"
        local pid
        pid="$(lsof -ti "tcp:$port" -sTCP:LISTEN 2>/dev/null || true)"
        if [[ -n "$pid" ]]; then
            echo "Stopping $name (pid $pid)"
            kill "$pid"
        fi
    done
    echo "Infra (RabbitMQ, per-service Postgres) left running. Use 'docker compose down' in each dir, or 'docker compose -f infra-compose.yml down', to stop them too."
}

cmd_status() {
    for entry in "${SERVICES[@]}"; do
        IFS=":" read -r name _ port <<< "$entry"
        if curl -s -o /dev/null "http://localhost:$port/"; then
            echo "UP    $name (port $port)"
        else
            echo "DOWN  $name (port $port)"
        fi
    done
}

case "${1:-}" in
    start) shift; cmd_start "${1:-}" ;;
    stop) cmd_stop ;;
    status) cmd_status ;;
    *)
        echo "Usage: $0 start [--build] | stop | status"
        exit 1
        ;;
esac
