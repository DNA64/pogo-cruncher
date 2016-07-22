(ns crunsher.utils.lib
  (:require [om.next :as om :refer-macros [defui]]
            [goog.dom :as gdom]
            [crunsher.data.pokemon :as pokemon]))

(defonce app-state
         (atom
           {:count   0
            :pokemon [{:pokemon_id         4
                       :id                 (rand-int 9999999999999999)
                       :individual_attack  10
                       :individual_defense 10
                       :individual_stamina 10}
                      {:pokemon_id         25
                       :id                 (rand-int 9999999999999999)
                       :individual_attack  10
                       :individual_defense 10
                       :individual_stamina 10}]}))


;;;; React compatibility
(defonce counter (atom 0))

(defn get-unique-key
  "Return unique react-key."
  []
  (str "crunsher-unique-react-key-" (swap! counter inc)))

(defn merge-react-key
  "Get a unique key, create a small map with :react-key property and merge it with the given collection."
  [col]
  (merge {:react-key (get-unique-key)} col))


;;;; Reconciler action
(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmulti mutate om/dispatch)

(defmethod mutate 'update/pokemon
    [{:keys [state]} _ {:keys [pokemon]}]
    {:action (fn [] (swap! state update-in [:pokemon] (fn [] pokemon)))})

(defmethod mutate 'increment
    [{:keys [state]} _ {:keys [pokemon]}]
    {:action (fn [] (swap! state update-in [:count] inc))})

(defonce reconciler
         (om/reconciler
           {:state  app-state
            :parser (om/parser {:read read :mutate mutate})}))

;;;; Get stuff
(defn inventory-pokemon
  "Return all pokemon which are currently stored."
  []
  (get-in @app-state [:pokemon]))

(defn get-pokemon-by-id
  "Look up database to return complete pokemon by its id."
  [pokemon-id]
  (get pokemon/all pokemon-id))


;;;; State transitions
(defn update-pokemon!
  "Update pokemon based on API response."
  [res]
  (om/transact! reconciler `[(update/pokemon {:pokemon ~res})]))