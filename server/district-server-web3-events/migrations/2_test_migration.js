const fs = require('fs');
const edn = require("jsedn");

const {last, copy, smartContractsTemplate} = require ("./utils.js");
const {contracts_build_directory, smart_contracts_path} = require ('../truffle.js');

// copy artifacts for placeholder replacements
copy ("MyContract", "MyContractCp", contracts_build_directory);
const MyContract = artifacts.require("MyContractCp");

/**
 * This migration deploys the test smart contract suite
 *
 * Usage:
 * truffle migrate --network ganache --reset
 */
module.exports = function(deployer, network, accounts) {

  const address = accounts [0];
  const gas = 4e6;
  const opts = {gas: gas, from: address};

  deployer
    .then (() => {
      console.log ("@@@ using Web3 version:", web3.version.api);
      console.log ("@@@ using address", address);
    })
    .then (() => deployer.deploy (MyContract, 1, Object.assign(opts, {gas: gas})))
    .then (myContract => {

      var smartContracts = edn.encode(
        new edn.Map([
          edn.kw(":my-contract"), new edn.Map([edn.kw(":name"), "MyContract",
                                               edn.kw(":address"), myContract.address])
        ]));

      console.log (smartContracts);
      fs.writeFileSync(smart_contracts_path, smartContractsTemplate (smartContracts, "test"));
    })
    .then (function () {
      console.log ("Done");
    });

}
