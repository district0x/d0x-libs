(ns district.ui.web3-tx-log.subs
  (:require
    [district.ui.web3-tx-log.queries :as queries]
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::txs
  queries/txs)

(reg-sub
  ::settings
  queries/settings)

(reg-sub
  ::open?
  queries/open?)