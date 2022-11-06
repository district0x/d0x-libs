(ns district.ui.smart-contracts.utils
  (:require [clojure.string :as str]
           [clojure.data.json :as json]))

(defn get-abi-from-truffle-art [json-path]
  (-> (slurp json-path)
      (json/read-str :key-fn keyword)
      :abi))

(defmacro slurp-env-contracts []
  (let [smart-contracts-file (System/getenv "SMART_CONTRACTS")
        smart-contracts-build-path (System/getenv "SMART_CONTRACTS_BUILD_PATH")
        skip-contracts (when-let [scs (System/getenv "SMART_CONTRACTS_SKIP")]
                         (->> (str/split scs #",")
                              (mapv keyword)))]
    (binding [*out* *err*]
      (println "SKIPPING CONTRACTS " skip-contracts)
      (if (and smart-contracts-file smart-contracts-build-path)
        (let [[_ _ smart-contracts-map] (->> (slurp smart-contracts-file)
                                             (format "[%s]")
                                             read-string
                                             second)
              contracts-to-load (apply dissoc smart-contracts-map skip-contracts)]
          (println "ADDING CONTRACTS ABIS INTO BUILD : " (keys contracts-to-load))
          (->> contracts-to-load
               (map (fn [[contract-key {:keys [name]}]]
                      [contract-key {:abi (get-abi-from-truffle-art (str smart-contracts-build-path
                                                                         "/"
                                                                         name
                                                                         ".json"))}]))
               (into {})))

        (do (println "WARNING: Please set SMART_CONTRACTS and SMART_CONTRACTS_FORMAT environment variables if you want to inject abis into bundle")
            {})))))
