(ns district.ui.web3-chain.queries)

(defn chain [db]
  (-> db :district.ui.web3-chain :chain))

(defn has-chain? [db]
  (boolean (seq (chain db))))

(defn assoc-chain [db chain]
  (assoc-in db [:district.ui.web3-chain :chain] chain))

(defn dissoc-web3-chain [db]
  (dissoc db :district.ui.web3-chain))
