#!/bin/sh
set -e

wait.sh elastic:9002 -- echo "elasticsearch is up"

wait.sh mariadb:3306 -- echo "mariadb is up"

$@
