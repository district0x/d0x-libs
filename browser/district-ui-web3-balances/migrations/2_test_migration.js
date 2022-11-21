const fs = require('fs');
const edn = require("jsedn");

const {copy, smartContractsTemplate} = require ("./utils.js");
const {contracts_build_directory, smart_contracts_path} = require ('../truffle.js');

// copy artifacts for placeholder replacements
copy ("SimpleERC20", "GNT", contracts_build_directory);
copy ("SimpleERC20", "ICN", contracts_build_directory);
copy ("SimpleERC20", "OMG", contracts_build_directory);
const GNT = artifacts.require("GNT");
const ICN = artifacts.require("ICN");
const OMG = artifacts.require("OMG");

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
    .then (() => deployer.deploy (GNT, "GNT", "GNT", web3.utils.toWei("1000000"), Object.assign(opts, {gas: gas})))
    .then (() => deployer.deploy (ICN, "ICN", "ICN", web3.utils.toWei("1000000"), Object.assign(opts, {gas: gas})))
    .then (() => deployer.deploy (OMG, "OMG", "OMG", web3.utils.toWei("1000000"), Object.assign(opts, {gas: gas})))
    .then (() => Promise.all ([GNT.deployed ()]))
    .then ((
      [GNT]) =>
        GNT.transfer("0x1111111111111111111111111111111111111111", web3.utils.toWei("100"))
    )
    .then (() => Promise.all ([GNT.deployed ()]))
    .then ((
      [GNT]) =>
        GNT.transfer("0x2222222222222222222222222222222222222222", web3.utils.toWei("150"))
    )
    .then (() => Promise.all ([ICN.deployed ()]))
    .then ((
      [ICN]) =>
        ICN.transfer("0x1111111111111111111111111111111111111111", web3.utils.toWei("122"))
    )
    .then (() => Promise.all ([OMG.deployed ()]))
    .then ((
      [OMG]) =>
        OMG.transfer("0x1111111111111111111111111111111111111111", web3.utils.toWei("177"))
    )
    .then (() => web3.eth.sendTransaction({to:"0x1111111111111111111111111111111111111111", from:address, value: web3.utils.toWei('1')}))
    .then (() => Promise.all ([GNT.deployed (), ICN.deployed(), OMG.deployed()]))
    .then ((
      [GNT, ICN, OMG]) => {

         var smartContracts = edn.encode(
           new edn.Map([

             edn.kw(":GNT"), new edn.Map([edn.kw(":name"), "GNT",
                                          edn.kw(":address"), GNT.address]),
             edn.kw(":ICN"), new edn.Map([edn.kw(":name"), "ICN",
                                          edn.kw(":address"), ICN.address]),
             edn.kw(":OMG"), new edn.Map([edn.kw(":name"), "OMG",
                                          edn.kw(":address"), OMG.address])
           ]));

         console.log (smartContracts);
         fs.writeFileSync(smart_contracts_path, smartContractsTemplate (smartContracts, address, "test"));
       })
    .catch(console.error);

  deployer.then (function () {
    console.log ("Done");
  });

}
