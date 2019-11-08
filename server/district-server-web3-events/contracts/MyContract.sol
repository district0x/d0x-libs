pragma solidity ^0.4.18;

contract MyContract {

  event SomeEvent(uint someParam);
  event SomeOtherEvent(uint someOtherParam);

  function MyContract() {
  }

  function fireSomeEvent(uint someParam) {
    SomeEvent(someParam);
  }

  function fireSomeOtherEvent(uint someOtherParam) {
    SomeOtherEvent(someOtherParam);
  }
}
