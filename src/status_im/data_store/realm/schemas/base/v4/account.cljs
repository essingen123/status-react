(ns status-im.data-store.realm.schemas.base.v4.account
  (:require [taoensso.timbre :as log]
            [cognitect.transit :as transit]
            [clojure.set :as set]))

(def schema {:name       :account
             :primaryKey :address
             :properties {:address               :string
                          :public-key            :string
                          :name                  {:type :string :optional true}
                          :email                 {:type :string :optional true}
                          :status                {:type :string :optional true}
                          :debug?                {:type :bool :default false}
                          :photo-path            :string
                          :signing-phrase        {:type :string}
                          :mnemonic              {:type :string :optional true}
                          :last-updated          {:type :int :default 0}
                          :last-sign-in          {:type :int :default 0}
                          :signed-up?            {:type    :bool
                                                  :default false}
                          :network               :string
                          :networks              {:type       :list
                                                  :objectType :network}
                          :last-request          {:type :int :optional true}
                          :settings              {:type :string}
                          :sharing-usage-data?   {:type :bool :default false}
                          :dev-mode?             {:type :bool :default false}
                          :seed-backed-up?       {:type :bool :default false}
                          :wallet-set-up-passed? {:type    :bool
                                                  :default false}}})

(def removed-tokens
  #{:ATMChain :Centra :ROL})

(def removed-fiat-currencies
  #{:bmd :bzd :gmd :gyd :kyd :lak :lrd :ltl :mkd :mnt :nio :sos :srd :yer})

(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn serialize [o] (transit/write writer o))
(defn deserialize [o] (try (transit/read reader o) (catch :default e nil)))

(defn migration [old-realm new-realm]
  (log/debug "migrating accounts schema v4")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)
            old-settings (deserialize (aget account "settings"))
            new-settings (-> old-settings
                             (update-in [:wallet :visible-tokens :mainnet]
                                        #(set/difference % removed-tokens))
                             (update-in [:wallet :currency]
                                        #(if (removed-fiat-currencies %) :usd %)))
            updated      (serialize new-settings)]
        (aset account "settings" updated)))))