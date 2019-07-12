(ns acme.frontend.core
  (:require [ajax.core :as ajax :refer [GET]]
            [ajax.edn :as aedn]
            [cljs.core.async :as async :refer [put! chan <! >! timeout close!]]
            [datascript.core :as d]
            [posh.reagent :refer [pull q posh!]]
            [reagent.core :as r]
            [cljs.reader :as reader]
            [taoensso.timbre :as timbre])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def counter (r/atom 0))
(def random-num (r/atom 0))

(def sse-chan (chan 15))

(def conn (d/create-conn {:sse/random-num {:db/unique :db.unique/identity}}))

(defn fun [f]
  (cond (= "+" f)
        (fn [b] (+ b))
        :else
        (fn [b] (- b))))

(defn data-q
  []
  (as-> (d/q '[:find ?num
               :where
               [?s :sse/event-name "sse-random-number"]
               [?s :sse/random-num ?num]] @conn) _
    (ffirst _)))

(defn data-handler
  [data]
  (d/transact! conn [{:db/ident :unique-key
                      :sse/event-name "sse-random-number"
                      :sse/random-num data}]))

(defn ^{:private true} events
  [url]
  (js/EventSource. url))

(defn event-source!
  []
  (let [url "http://192.168.1.136:8890/sse-random"]
    (.addEventListener (events url)
                       "sse-random"
                       (fn [e]
                         (go (>! sse-chan (js->clj e))))
                       false)))

(defn read-sse
  []
  (event-source!)
  (go-loop []
    (when-let [msg (<! sse-chan)]
      (cond (nil? msg)
            (close! sse-chan)
            :else
            (do (data-handler (-> msg .-data js/parseFloat))
                (reset! random-num (data-q)))))
    (recur)))

(defn ^:export CountClicks
  []
  (r/create-class
   {:reagent-render
    (fn [props]
      [:div.custom
       [:h1 @counter]
       [:h2 props]
       [:button {:on-click (fn [] (swap! counter inc))} "Click me"]])}))

(defn ^:export Howdy
  [props]
  (r/create-class
   {:reagent-render
    (fn [props]
      [:button {:on-click #(js/alert "Howdy Y'all! This alert was made in Clojurescript")} "show me"])}))

(defn ^:export Welcome
  []
  (r/create-class
   {:reagent-render
    (fn [props]
      [:div
       [:h1 "Howdy, Y'all!"] (:name props)])}))

(defn ^:export PrintDatascript
  []
  (r/create-class
   {:reagent-render
    (fn [props]
      [:div
       [:button {:on-click #(js/console.log (data-q))} "Show datascript on console"]])}))

(def ^:export Init (r/reactify-component [:<> (read-sse)]))

(defn ^:export ShowRandomNumber
  []
  (r/create-class
   {:component-did-mount
    (fn [props])
    :reagent-render
    (fn [{:keys [title func]}]
      (let [f (fun func)]
        [:div
         [:h2 title]
         [:h5 (-> @random-num f)]]))}))