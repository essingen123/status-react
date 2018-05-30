(ns status-im.data-store.realm.schemas.base.v4.core
  (:require [status-im.data-store.realm.schemas.base.v1.network :as network]
            [status-im.data-store.realm.schemas.base.v4.account :as account]
            [taoensso.timbre :as log]))

(def schema [network/schema
             account/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating base database v4: " old-realm new-realm)
  (account/migration old-realm new-realm))
