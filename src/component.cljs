(ns tidy-todo-v8
  (:require
   [reagent.core :as r]
   [roam.block :as block]
   [datascript.core :as d]
   [roam.datascript.reactive :as dr]
   [clojure.string :as str]
   [clojure.pprint :refer [pprint]]
   [promesa.core :as p]
   [roam.util :refer [parse]]
   [blueprintjs.core :as bp]
   [roam.datascript :as rd]
   ))

; THIS CODEBLOCK IS OVERWRITTEN ON EVERY VERSION UPDATE
; DO NOT MODIFY

(defonce bp-button (r/adapt-react-class bp/Button))

(defn find-blocks-that-ref
  "Returns all _refs for each children block given a parent block uid" 
  [block-uid ref-title]
  (dr/q '[:find (pull ?node [:block/order :block/string :block/uid])
          :in $ ?uid ?ref-page-name
          :where
          [?e :block/uid ?uid]
          [?e :block/children ?node]
          [?node :block/refs ?DONE-Ref]
          [?DONE-Ref :node/title ?ref-page-name]]
    block-uid ref-title))

(defn get-string [item]
  (:block/string (first item)))

(defn sort-by-string [data]
  (sort-by #(str/lower-case (get-string %)) data))

(defn move-to-end [parent-uid item]
  (block/move
    {:location {:parent-uid parent-uid
                :order "last"}
     :block {:uid (:block/uid (first item))}}))

(defn sorted-data [data]
  (->> (deref data)
       (sort-by #(get-in (first %) [:block/order]))
  ))

(defn sort-all-todos[parent-uid]
  
  (r/with-let [
               TODOs (find-blocks-that-ref parent-uid "TODO")
               DONEs (find-blocks-that-ref parent-uid "DONE")
               ARCHIVEDs (find-blocks-that-ref parent-uid "ARCHIVED")
              ]
    (p/run!
      (fn [item]
        (prn (first item))
        (block/move
          {:location {:parent-uid parent-uid
                      :order "last"}
           :block {:uid (:block/uid (first item))}}))
      (concat
        ;;sort TODOs by order not creation date
        (sorted-data TODOs)
        ;;sort alphabetically          
        (sort-by-string @DONEs)
        (sort-by-string @ARCHIVEDs)
        )
      )
    )
  )

;; functions for removing the tidy
(defn clean-tidy-string [input-string]
  (let [pattern #"\{\{\[\[roam/render\]\]\:.*?\}\}"
        cleaned-string (clojure.string/replace input-string pattern "")]
    cleaned-string))

(defn get-block-string [uid]
  (:block/string (rd/pull 
      '[:block/string 
        ] 
      [:block/uid uid]))
)

(defn update-block-string
  [block-uid block-string]
  (block/update 
      {:block {:uid block-uid 
               :string block-string}})
  )

(defn remove-tidy [block-uid]
  (->> block-uid
    (get-block-string )
    (clean-tidy-string)
    (update-block-string block-uid)
    )
  )

  (defn button [block-uid]
    [bp-button 
        {:small true
         :outlined false
         :minimal true
         :icon "sort"
         :class "tidyButton"
         :style{ :margin-right "4px"}
         :title "Click to tidy this list"
         :on-click (fn [e]
                     (sort-all-todos block-uid))
       }]
)
(defn delete-button [block-uid]
    [bp-button 
        {:small true
         :outlined false
         :minimal true
         :icon "small-cross"
         :class "tidyRemoveButton"
         :style{ :margin-right "4px"}
         :title "Remove the tidy component"
         :on-click (fn [e]
                     (remove-tidy block-uid))
       }]
)

(defn main [{:keys [block-uid]} & args]
    [:div
       (parse (str "#[[" (first args) "]]")) 
        [button block-uid]
        [delete-button block-uid]
        ]
    )