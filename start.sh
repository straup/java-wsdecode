#!/bin/sh
# $Id: start.sh,v 1.5 2008/02/23 08:33:11 asc Exp $

PORT=9955
IAMHERE=`dirname $0`
java -cp ${IAMHERE}/lib/core.jar:${IAMHERE}/lib/javase.jar:${IAMHERE}/lib/openjdk_071012-httpserver.jar:${IAMHERE}/lib/barcode.jar info.aaronland.barcode.WsDecode ${PORT}
