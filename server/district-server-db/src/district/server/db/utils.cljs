(ns district.server.db.utils)


(defn total-count-query [sql-map & [{:keys [:count-distinct-column :count-select]
                                     :or {count-select [:%count.*]}}]]
  (let [select (if (contains? (set (:modifiers sql-map)) :distinct)
                 [(sql/call :count-distinct (or count-distinct-column (first (:select sql-map))))]
                 count-select)]
    (-> sql-map
        (assoc :select select)
        (dissoc :offset :limit :order-by))))

(defn order-by-similarity [col-name s & [{:keys [:suffix :prefix]}]]
  (sql/call :case
            [:= col-name (str prefix s suffix)] 1
            [:like col-name (str prefix s "%" suffix)] 2
            [:like col-name (str prefix "%" s "%" suffix)] 3
            :else 4))
