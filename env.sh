#!/usr/bin/env bash


export YOUR_IOT_PLATFORM_NAME=iot-pipes
pip install git+https://github.com/ibm-watson-iot/functions.git@production --upgrade

ibmcloud api https://api.ng.bluemix.net

ibmcloud service create iotf-service iotf-service-free ${YOUR_IOT_PLATFORM_NAME}
echo "see https://developer.ibm.com/iotplatform/2017/12/07/use-device-simulator-watson-iot-platform/ for info on simulating devices"
