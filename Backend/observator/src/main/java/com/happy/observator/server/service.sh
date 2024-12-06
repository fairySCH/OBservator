#!/bin/bash

ssh -v fairy@14.32.188.229 << EOF
conda activate tf
cd ~/ml/crypto_maniac
/home/fairy/yes/envs/tf/bin/python3 ./service.py
EOF
