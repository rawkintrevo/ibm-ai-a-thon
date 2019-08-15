#!/usr/bin/env bash


docker run --rm -v "$PWD:/tmp" openwhisk/python3action bash \
  -c "cd tmp && virtualenv virtualenv && source virtualenv/bin/activate && pip install -r requirements.txt"

zip -r action.zip virtualenv __main__.py virtualenv lib
ibmcloud wsk action update aiathon/datashim --kind python:3.6 action.zip