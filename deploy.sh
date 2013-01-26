#!/bin/sh

set -e

version="1.0"

mkdir beta-$version

cp -pr bin beta-$version
cp -pr data beta-$version
cp -pr dist beta-$version

tar czf beta-$version.tar.gz beta-$version

rm -r beta-$version
