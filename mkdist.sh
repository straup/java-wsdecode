#!/bin/sh
# $Id: mkdist.sh,v 1.10 2008/02/24 17:30:25 asc Exp $

VERSION=$1

IAMHERE=`dirname $0`
OLDCWD=`pwd`
DIST='ws-decode-'${VERSION}

# tie our shoe laces

cd ${IAMHERE}
ant -f build.xml

curl http://localhost:9955 > ./README

# bundle bundle bundle

mkdir -p ./${DIST}/lib
mkdir -p ./${DIST}/src/info/aaronland/barcode

cp ./start.sh ./${DIST}/
cp ./README ./${DIST}/
cp ./Changes ./${DIST}/

cp ./lib/*.jar ./${DIST}/lib/
cp ./build/dist/lib/*.jar ./${DIST}/lib/
cp ./src/info/aaronland/barcode/WsDecode.java ./${DIST}/src/info/aaronland/barcode

tar -cvzf ./${DIST}.tar.gz ./${DIST}

cd ${OLDCWD}