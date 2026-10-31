#!/bin/sh
set -eu

case "${DATABASE_URL:-}" in
  postgres://*)
    DATABASE_URL="jdbc:postgresql://${DATABASE_URL#postgres://}"
    export DATABASE_URL
    ;;
  postgresql://*)
    DATABASE_URL="jdbc:${DATABASE_URL}"
    export DATABASE_URL
    ;;
esac

case "${EVALUATOR_URL:-}" in
  ""|http://*|https://*) ;;
  *)
    EVALUATOR_URL="http://${EVALUATOR_URL}"
    export EVALUATOR_URL
    ;;
esac

exec java -jar /app/app.jar
