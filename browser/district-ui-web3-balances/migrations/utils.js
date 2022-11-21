const fs = require('fs');

const utils = {

  last: (array) => {
    return array[array.length - 1];
  },

  copy: (srcName, dstName, contracts_build_directory, network, address) => {

    let buildPath = contracts_build_directory;

    const srcPath = buildPath + srcName + '.json';
    const dstPath = buildPath + dstName + '.json';

    const data = require(srcPath);
    data.contractName = dstName;

    // Save address when given
    if (network && address) {
      data.networks = {};

      // Copy existing networks
      if (fs.existsSync(dstPath)) {
        const existing = require(dstPath);
        data.networks = existing.networks;
      }

      data.networks[network.toString()] = {
        address: address
      };
    }
    fs.writeFileSync(dstPath, JSON.stringify(data, null, 2), { flag: 'w' });
  },

  smartContractsTemplate (map, creator, env) {
    return `(ns tests.smart-contracts-test)

(def smart-contracts
  ${map})

(def creator
  "${creator}")
`;
  }

};

module.exports = utils;
