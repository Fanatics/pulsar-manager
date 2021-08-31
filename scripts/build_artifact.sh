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
tar -czvf pubsub-pulsar-manager-$version-bin.tar.gz pulsar-manager dist