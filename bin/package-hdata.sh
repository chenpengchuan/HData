#!/usr/bin/env bash


CDPATH=""
SCRIPT="$0"

while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$SCRIPT"`/"$link"
  fi
done

HDATA_HOME=`dirname "$SCRIPT"`/..
HDATA_HOME=`cd "$HDATA_HOME"; pwd`
HDATA_BIN_DIR=$HDATA_HOME/bin
HDATA_CONF_DIR=$HDATA_HOME/conf
HDATA_BUILD_DIR=$HDATA_HOME/build
HDATA_BUILD_HDATA_DIR=$HDATA_HOME/build/hdata

if [ -d "$HDATA_BUILD_DIR" ]; then
  rm -rf $HDATA_BUILD_DIR/*
fi

mkdir -p $HDATA_BUILD_HDATA_DIR/lib
mkdir -p $HDATA_BUILD_HDATA_DIR/bin
mkdir -p $HDATA_BUILD_HDATA_DIR/plugins
cp $HDATA_HOME/bin/hdata* $HDATA_BUILD_HDATA_DIR/bin/
cp -r $HDATA_HOME/conf $HDATA_BUILD_HDATA_DIR/

mvn clean package -Pcopy-dependency -DskipTests

cp $HDATA_HOME/hdata-core/target/hdata-core-*.jar $HDATA_BUILD_HDATA_DIR/lib
cp $HDATA_HOME/hdata-core/target/dependency/*.jar $HDATA_BUILD_HDATA_DIR/lib
cp $HDATA_HOME/hdata-api/target/dependency/*.jar $HDATA_BUILD_HDATA_DIR/lib

for f in $HDATA_HOME/hdata-*; do
    if [ $f != $HDATA_HOME/hdata-api -a $f != $HDATA_HOME/hdata-core ]; then
       pluginDir=$HDATA_BUILD_HDATA_DIR/plugins/${f##*-}
       mkdir -p $pluginDir
       cp $f/target/hdata-*.jar $pluginDir
       cp $f/target/dependency/*.jar $pluginDir
       if [ -d  $f/lib ]; then
        cp $f/lib/*.jar $pluginDir
       fi
    fi
done

cd $HDATA_BUILD_DIR

EXCLUDE_LIST="csv excel hbase hdfs hive kafka"
for i in $EXCLUDE_LIST
do
  echo $i
  rm -rf $HDATA_BUILD_HDATA_DIR/plugins/$i
done

FINAL_NAME=hdata-`date +%y-%m-%d`.zip
zip -b $HDATA_BUILD_DIR -r $FINAL_NAME hdata


