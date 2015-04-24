(ns om-tut.item
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]))

(defcomponent todo-checkbox
  [todo owner]
  (render [_]
    (let [toggle (fn [todo] (update todo :done not))]
      (html [:input {:type "checkbox"
                      :checked (:done todo)
                      :on-click #(om/transact! todo toggle)}]))))

(defn toggle-todo-edit! [owner] (om/set-state! owner :editing? (not (om/get-state owner :editing?))))

(defn save-edited-todo! [todo value] (om/update! todo :text value))

(defcomponent delete-todo
  [todo owner opts]
  (render [_]
          (html [:button "Delete me"])
))

(defcomponent todo-text
  [todo owner]
  (init-state [_] {:editing? false})
  (render-state [_ {:keys [editing?]}]
    (let [cls (if (:done todo) "done" "")]
    (html [:span (if editing?
                   [:input {:type "text"
                            :default-value (:text todo)
                            :on-blur (fn [event]
                                       (let [input (.-target event)]
                                        (toggle-todo-edit! owner)
                                        (save-edited-todo! todo (.-value input))))
                            :on-key-up (fn [event] (
                                     let [input (.-target event)]
                                     (case (.-keyCode event)
                                       13 (do (toggle-todo-edit! owner) (save-edited-todo! todo (.-value input)))
                                       27 (toggle-todo-edit! owner)
                                       identity)))}]
                   [:span {:class cls
                           :on-double-click #(toggle-todo-edit! owner)}
                    (:text todo)])]))))

(defcomponent todo-item
  [todo owner opts]
  (render [_]
     (html [:li {:class "flex-item"}
            (om/build todo-checkbox todo)
            (om/build todo-text todo)
            (om/build delete-todo todo)
            ])))
