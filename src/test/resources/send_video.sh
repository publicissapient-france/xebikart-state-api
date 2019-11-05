#! /bin/bash

# shellcheck disable=SC2045
for file in video/tub/*.jpg;
do
  mosquitto_pub -h rabbitmq.xebik.art -u xebikart1 -P xebikart1 -p 1883 -t xebikart-car-video -d -q 0 -m "$(cat $file | base64)"
done