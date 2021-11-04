(ns district.ui.web3-chain.subs
  (:require
    [district.ui.web3-chain.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::chain
  queries/chain)

(reg-sub
  ::has-chain?
  queries/has-chain?)
