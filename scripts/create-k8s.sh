#!/usr/bin/env bash

echo "Creating VM"
multipass launch bionic -n aiathon -m 8G -d 40G -c 4
 multipass mount ./sharedata aiathon:/home/multipass/sharedata
multipass shell aiathon

echo "Setting up microK8s"
# https://tutorials.ubuntu.com/tutorial/install-a-local-kubernetes-with-microk8s#1
git clone https://github.com/canonical-labs/kubernetes-tools
sudo kubernetes-tools/setup-microk8s.sh
microk8s.enable dns dashboard istio registry
sudo snap install docker
kubernetes-tools/expose-dashboard.sh > ./sharedata/dashboard.txt
echo "get your <EXTERNAL IP> by running 'multipass ls' from outside of multipass" >> ./sharedata/dashboard.txt
# Think i need to turn on docker support...

echo "Setting up Flink"
# wget these? edit these?
# https://ci.apache.org/projects/flink/flink-docs-stable/ops/deployment/kubernetes.html
kubectl create -f jobmanager-service.yaml
kubectl create -f jobmanager-deployment.yaml
kubectl create -f taskmanager-deployment.yaml
# Run kubectl proxy in a terminal
echo "Navigate to http://<EXTERNAL IP>>:8001/api/v1/namespaces/default/services/flink-jobmanager:ui/proxy in your browser" >> ../sharedata/dashboard.txt


#echo "Setting up Kubeflow,  do you need this? Why not just watsonML?"
## https://www.kubeflow.org/docs/started/getting-started-multipass/
#git clone https://github.com/canonical-labs/kubeflow-tools
#kubeflow-tools/install-kubeflow.sh

