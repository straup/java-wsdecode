#!/bin/sh
# $Id: run.sh,v 1.5 2008/02/23 08:33:11 asc Exp $

PORT=9956

IAMHERE=`dirname $0`
OLDCWD=`pwd`

cd ${IAMHERE}
ant -f build.xml
cd ${OLDPWD}

PID=`ps auxwww | /usr/bin/grep WsDecode | /usr/bin/grep ${PORT} | /usr/bin/grep -v grep | awk '{ print $2}'`
kill -9 ${PID}

java -cp ${IAMHERE}/lib/core.jar:${IAMHERE}/lib/javase.jar:${IAMHERE}/lib/openjdk_071012-httpserver.jar:${IAMHERE}/build/dist/lib/barcode.jar info.aaronland.barcode.WsDecode ${PORT}
