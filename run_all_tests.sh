#!/usr/bin/env bash

BROWSER=$1
ENVIRONMENT=$2

sbt -J-Xmx3G -Dbrowser="${BROWSER:=chrome}" -Denvironment="${ENVIRONMENT:=local}" pre-commit
