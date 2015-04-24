(ns ^:figwheel-always om-tut.core
  (:require[om.core :as om :include-macros true]
           [sablono.core :refer-macros [html]]
           [alandipert.storage-atom :refer [local-storage]]
           [om-tut.item :refer [todo-item]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def filters {
              :all {:text "All" :filter (fn [todo] true)}
              :active {:text "Active" :filter (complement :done)}
              :completed {:text "Completed" :filter :done}
              })



(defonce app-state
 ; (local-storage
   (atom {:todo-list
          [{:text "Get ClojureScript setup" :done true :level "easy"}
           {:text "Make first Om Component" :done true :level "easy"}
           {:text "Become an om ninja" :done false :level "hard"}]
          :current-filter (:filter (:all filters))}

  ;  )
 ;  :todo-app-state
   )
  )

(defn add-todo [text todos]
  (conj todos {:text text :done false})
  )

(defn not-done-todos-count [todos] (count (remove :done todos)))

(defn done-todos-count [todos] (count (filter :done todos)))

(defn todo-adder
  [todos owner]
  (om/component
   (html
    [:div {:class "flex-item"} [:input {
             :type "text"
             :class "add-todo"
             :placeholder "What needs to be done?"
             :on-key-up (fn [event] (
                                     let [input (.-target event)]
                                     (when (= 13 (.-keyCode event))
                                       (om/transact! todos
                                                     (partial add-todo (.-value input)))
                                       (set! (.-value input) "")
                                       )
                                     )
                          )
             }
     ]
     ]
    )
   )
  )

(defn todos-clear
  [todos owner]
  (om/component
   (let [clear-done-todos (fn [todos] (vec (remove :done todos)))]
     (html
      [:a {
           :class "flex-item"
           :disabled (if (= (done-todos-count todos) 0) true false)
           :on-click #(om/transact! todos clear-done-todos)}
       "Clear completed"]))))

(defn mark-all-todos-as-done [todos owner] (om/component
    (html [:div {:class "flex-item"}
           [:button {:on-click #(om/transact! todos (fn [todos] (vec (map (fn [todo] (assoc todo :done true)) todos))))} "Mark all todos as done"]]))
)

(defn display-filters
  [data owner]
  (om/component
   (html
    [:div
     (for [todo-filter filters]
       [:a
        {:class (if (= (:current-filter data) (:filter (nth todo-filter 1))) "todo-filter active" "todo-filter")
         :on-click #(om/transact! data :current-filter (fn [current-filter] (:filter (nth todo-filter 1))))}
        (:text (nth todo-filter 1))])])))

(defn todos-bottom-bar
  [data owner]
  (om/component
     (html
      [:div {:class "bottom-bar flex-item container"}
       [:div {:class "flex-item count"}
        [:span (not-done-todos-count (:todo-list data))]
        [:span " items left"]]
       (om/build display-filters data)
       (om/build todos-clear (:todo-list data))
       ])))

(defn todos-component
  [data owner]
  (om/component
   (html
    [:div
     [:h1 "todos"]
     (om/build mark-all-todos-as-done (:todo-list data))
     (om/build todo-adder (:todo-list data))
     [:ul
      (om/build-all todo-item (filterv (:current-filter data) (:todo-list data) ))]
     (om/build todos-bottom-bar data)])))

(om/root todos-component
         app-state
         {:target (. js/document (getElementById "todos"))})
