#!/usr/bin/env bash
##!/usr/bin/env bash


echo "Setting up Flink"
# https://ci.apache.org/projects/flink/flink-docs-stable/ops/deployment/kubernetes.html
kubectl create -f k8s/flink/jobmanager-service.yaml
kubectl create -f k8s/flink/jobmanager-deployment.yaml
kubectl create -f k8s/flink/taskmanager-deployment.yaml
# Run kubectl proxy in a terminal
echo "Navigate to http://<EXTERNAL IP>>:8001/api/v1/namespaces/default/services/flink-jobmanager:ui/proxy in your browser" >> k8s/dashboard.txt
# https://www.elastic.co/elasticsearch-kubernetes
echo "Setting up Elasticsearch"
kubectl run elasticsearch --image=docker.elastic.co/elasticsearch/elasticsearch:6.8.1 --port=9200 -- elasticsearch -E discovery.type=single-node
kubectl expose deployment/elasticsearch --type=LoadBalancer

echo "setting up kibana"
kubectl run kibana --image=docker.elastic.co/kibana/kibana:6.8.1 \
  --port=5601 \
  --env="PUBLISH_HOST=kibana.default" \
  --env="SERVER_HOST=0.0.0.0"

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
kubectl apply -f k8s/frontend/my-frontend.yaml
kubectl expose deployment/my-frontend

echo "Creating Ingress"
kubectl apply -f k8s/frontend/myingress.yaml
kubectl apply -f k8s/frontend/myingress2.yaml
kubectl apply -f k8s/frontend/myingress3.yaml
kubectl apply -f k8s/frontend/myingress4.yaml

kubectl exec -it flink-jobmanager-c84568b5d-l82zk -- bash
echo "rest.server.max-content-length: 209715200" >> conf/flink-conf.yaml
jobmanager.sh start-foreground jobmanager
curl -X POST -H "Expect:" -F "jarfile=@/flink-runtime/target/flink-runtime-1.0-SNAPSHOT.jar" http://flink.ai-a-thon.us-south.containers.appdomain.cloud/jars/upload
