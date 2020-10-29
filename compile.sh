#!/bin/sh

path_src_jh="src/copy-json4java8/java/com"
path_src="src/main/java/com"
path_bin="bin"
path_export_jar="Export.jar"
version="8"

# remove bin files
rm -Rf ${path_bin}

# mkdir bin
mkdir ${path_bin}

# compile-jsonhub
javac -d ${path_bin} \
--release ${version} \
$(find ${path_src_jh} -name "*.java")

# compile-main
javac -d ${path_bin} \
--class-path ${path_bin} \
--release ${version} \
$(find ${path_src} -name "*.java")

# jar
jar -c \
-f ${path_export_jar} \
-C ${path_bin} .

