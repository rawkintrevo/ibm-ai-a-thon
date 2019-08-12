
cd ..
echo "Setting up microK8s"
# https://tutorials.ubuntu.com/tutorial/install-a-local-kubernetes-with-microk8s#1
git clone https://github.com/canonical-labs/kubernetes-tools
sudo kubernetes-tools/setup-microk8s.sh
microk8s.enable dns dashboard istio registry
microk8s.status # check that dashboard is enabled
sudo snap install docker
kubernetes-tools/expose-dashboard.sh > ./sharedata/dashboard.txt
echo "get your <EXTERNAL IP> by running 'multipass ls' from outside of multipass" >> ./sharedata/dashboard.txt

sudo iptables -P FORWARD ACCEPT

