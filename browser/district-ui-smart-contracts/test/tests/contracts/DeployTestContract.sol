pragma solidity ^0.4.18;

contract DeployTestContract {
  function DeployTestContract(uint someNumber) public {
    require(someNumber != 0);
  }
}
