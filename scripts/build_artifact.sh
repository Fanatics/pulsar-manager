#!/bin/sh -xe

version=$1
if [[ -z $version ]]; then
    version=1.0.0
fi

echo "Building backend"

./gradlew build -x test

echo "Building frontend"

cd front-end

npm run build:prod

echo "Creating artifact"
cd -

mkdir -p artifacts

cd artifacts

cp -r ../front-end/dist dist
tar -xf ../build/distributions/pulsar-manager.tar

echo "remove JndiLookup.class from log4j-core"
zip -q -d pulsar-manager/lib/log4j-core-*.jar org/apache/logging/log4j/core/lookup/JndiLookup.class && echo "removed JndiLookup.class"
echo "remove JndiManager.class from log4j-core"
zip -q -d pulsar-manager/lib/log4j-core-*.jar org/apache/logging/log4j/core/net/JndiManager.class && echo "removed JndiManager.class"

tar -czvf pubsub-pulsar-manager-$version-bin.tar.gz pulsar-manager dist