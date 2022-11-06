#!/usr/bin/env bash
cd test/tests/contracts

solc --overwrite --optimize --bin --abi DeployTestContract.sol -o ./

wc -c DeployTestContract.bin | awk '{print "DeployTestContract: " $1}'
