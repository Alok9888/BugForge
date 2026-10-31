#!/bin/sh
set -eu

: "${BACKEND_HOSTPORT:?Set BACKEND_HOSTPORT to the private backend host and port}"
envsubst '${BACKEND_HOSTPORT}' < /etc/nginx/default.conf.template > /etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'
