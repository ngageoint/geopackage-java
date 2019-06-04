#!/bin/sh
set -f
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
java -jar ${__dir}/geopackage-standalone.jar $*
