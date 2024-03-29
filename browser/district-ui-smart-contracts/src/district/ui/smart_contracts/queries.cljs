(ns district.ui.smart-contracts.queries
  (:require
    [cljs-web3-next.eth :refer [contract-at]]
    [district.ui.web3.queries :as web3-queries]
    [clojure.string :as string]))

(defn contracts [db]
  (:district.ui.smart-contracts db))

(defn contract [db contract-key]
  (-> db :district.ui.smart-contracts contract-key))

(defn contract-address [db contract-key]
  (:address (contract db contract-key)))

(defn contract-abi [db contract-key]
  (:abi (contract db contract-key)))

(defn contract-bin [db contract-key]
  (:bin (contract db contract-key)))

(defn contract-name [db contract-key]
  (:name (contract db contract-key)))

(defn instance
  ([db contract-key] ; :ethlance
   (let [contr (contract db contract-key)
         address (contract-address db contract-key)]
     (if-not (:forwards-to contr)
       (instance db contract-key address)
       (instance db (:forwards-to contr) (contract-address db contract-key)))))
  ([db contract-abi-key contract-address-key-or-address]
   (when-let [web3 (web3-queries/web3 db)]
     (contract-at web3
                  (contract-abi db contract-abi-key)
                  (if (keyword? contract-address-key-or-address)
                    (contract-address db contract-address-key-or-address)
                    contract-address-key-or-address)))))


(defn merge-contracts [db contracts]
  (update db :district.ui.smart-contracts merge contracts))

(defn merge-contract [db contract-key contract]
  (update-in db [:district.ui.smart-contracts contract-key] merge contract))

(defn assoc-contract-abi [db contract-key abi]
  (assoc-in db [:district.ui.smart-contracts contract-key :abi]
            (cond
              (string? abi) (js/JSON.parse (clj->js abi))
              (array? abi) abi
              (vector? abi) (clj->js abi)
              :else nil)))

(defn assoc-contract-bin [db contract-key bin]
  (assoc-in db
            [:district.ui.smart-contracts contract-key :bin]
            (str (when-not (string/starts-with? bin "0x") "0x") bin)))

(defn dissoc-smart-contracts [db]
  (dissoc db :district.ui.smart-contracts))
