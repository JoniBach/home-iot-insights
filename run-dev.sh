#!/bin/sh
set -eu

if [ -f .env ]; then
  set -a
  source .env
  set +a
fi

exec sbt run