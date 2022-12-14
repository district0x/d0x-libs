(ns district.ui.web3.utils)


(defn web3-injected?
  "Determines if the `web3` object has been injected by an
  ethereum provider."
  []
  (boolean (or (aget js/window "ethereum" ) (aget js/window "web3"))))


(defn web3-legacy?
  "The old method of retrieving the current ethereum provider exposed
  it at `window.web3.currentProvider`..

  Notes:

  - This changed in EIP-1102 to require authorization, and moved the
  partial provider into `window.ethereum`.

  - Can assume it isn't legacy when window.ethereum exists."
  []
  (not (some-> js/window .-ethereum)))
