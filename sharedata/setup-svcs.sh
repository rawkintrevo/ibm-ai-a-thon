##!/usr/bin/env bash


echo "Setting up Flink"
# wget these? edit these?
# https://ci.apache.org/projects/flink/flink-docs-stable/ops/deployment/kubernetes.html
kubectl create -f sharedata/flink/jobmanager-service.yaml
kubectl create -f sharedata/flink/jobmanager-deployment.yaml
kubectl create -f sharedata/flink/taskmanager-deployment.yaml
# Run kubectl proxy in a terminal
echo "Navigate to http://<EXTERNAL IP>>:8001/api/v1/namespaces/default/services/flink-jobmanager:ui/proxy in your browser" >> sharedata/dashboard.txt
# https://www.elastic.co/elasticsearch-kubernetes
echo "Setting up Elasticsearch"
#kubectl apply -f https://download.elastic.co/downloads/eck/0.8.1/all-in-one.yaml
#cat <<EOF | kubectl apply -f -
#apiVersion: elasticsearch.k8s.elastic.co/v1alpha1
#kind: Elasticsearch
#metadata:
#  name: aiathon
#spec:
#  version: 6.8.1
#  nodes:
#  - nodeCount: 1
#    config:
#      node.master: true
#      node.data: true
#      node.ingest: true
#EOF
#cat <<EOF | kubectl apply -f -
#apiVersion: kibana.k8s.elastic.co/v1alpha1
#kind: Kibana
#metadata:
#  name: aiathon
#spec:
#  version: 6.8.1
#  nodeCount: 1
#  elasticsearchRef:
#    name: aiathon
#EOF


#kubectl run elasticsearch --image=docker.elastic.co/elasticsearch/elasticsearch:6.8.1 --env="discovery.type=single-node" --port=9200
# ^ doesn't work in IBM, but this hack does.
kubectl run elasticsearch --image=docker.elastic.co/elasticsearch/elasticsearch:6.8.1 --port=9200 -- elasticsearch -E discovery.type=single-node
kubectl expose deployment/elasticsearch --type=LoadBalancer

echo "setting up kibana"
kubectl run kibana --image=docker.elastic.co/kibana/kibana:6.8.1 \
  --port=5601 \
  --env="PUBLISH_HOST=kibana.default" \
  --env="SERVER_HOST=0.0.0.0"
#  --env="PUBLISH_HOST=kibana.default" \
#  --env="SERVER_BASEPATH=/api/v1/namespaces/default/services/kibana:5601/proxy"

kubectl expose deployment/kibana # --type=LoadBalancer

echo "now chill out for a bit bc it will take a minute for all this to setup."
echo "put a check loop here for 'Running' in kubectl get pods | grep aiathon-kibana"

# Busybox is good for debugging
#
#cat <<EOF | kubectl apply -f -
#apiVersion: v1
#kind: Pod
#metadata:
#  name: busybox
#  namespace: default
#spec:
#  containers:
#  - name: busybox
#    image: busybox:1.28
#    command:
#      - sleep
#      - "3600"
#    imagePullPolicy: IfNotPresent
#  restartPolicy: Always
#EOF

echo "Creating WebUI"
kubectl apply -f sharedata/frontend/my-frontend.yaml
kubectl expose deployment/my-frontend

echo "Creating Ingress"
kubectl apply -f sharedata/frontend/myingress.yaml
kubectl apply -f sharedata/frontend/myingress2.yaml
kubectl apply -f sharedata/frontend/myingress3.yaml
