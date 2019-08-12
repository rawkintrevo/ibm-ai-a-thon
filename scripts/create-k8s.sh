#!/usr/bin/env bash

echo "Creating VM"
multipass launch bionic -n aiathon -m 10G -d 40G -c 4
echo "mounting ../sharedata"
multipass mount ../sharedata aiathon:/home/multipass/sharedata
echo "attempting to enter shell"
multipass shell aiathon
echo "i'm in the shell maybe?"

