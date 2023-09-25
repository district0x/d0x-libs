(ns district.sendgrid
  (:require
    ["xhr2" :as xhr2]
    [cljs-http.client :as http]
    [cljs.core.async :as async]))

; cljs-http uses google Closure goog.net.XhrIo which works on browsers but
; needs XMLHttpRequest on Node.js
; Solution from: https://github.com/r0man/cljs-http/issues/94
(set! js/XMLHttpRequest xhr2)

(defonce ^:private sendgrid-public-api "https://api.sendgrid.com/v3/mail/send")

; Sendgrid API documentation:
;   https://docs.sendgrid.com/api-reference/mail-send/mail-send
(defn send-email
  [{:keys [:from :to :subject :content :substitutions :on-success :on-error :template-id :api-key :body :headers
           :dynamic-template-data :print-mode?]}]
  (if (and (not api-key)
           (not print-mode?))
    (throw (js/Error. "Missing api-key to send email to sendgrid"))
    (if print-mode?
      (do
        (println "Would send email:")
        (println "From:" from)
        (println "To:" to)
        (println "Subject:" subject)
        (println "Content:" content)
        (println "Substitutions:" substitutions)
        (println "Dynamic-template-data:" dynamic-template-data))
      (let [headers (merge {"Authorization" (str "Bearer " api-key)
                            "Content-Type" "application/json"} headers)
            body (merge {:from {:email from}
                         :personalizations [(cond-> {:to [{:email to}]}
                                                    dynamic-template-data (assoc :dynamic_template_data dynamic-template-data)
                                                    ;; Substitutions are in format e.g ":header", so (str :header) works well
                                                    substitutions (assoc :substitutions (into {} (map (fn [[k v]] [(str k) v]) substitutions))))]
                         :subject subject
                         :content [{:type "text/html"
                                    :value content}]
                         :template_id template-id}
                        body)]
        (async/take! (http/post sendgrid-public-api {:headers headers :json-params body})
                     (fn [res] (if (= true (:success res))
                                 (on-success res)
                                 (on-error res))))
        ))))
