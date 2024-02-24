(ns plutonia.docs.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [clojure.string :as string]
   [plutonia.ui :as ui]
   [plutonia.views-common :as views]
   [plutonia.util :as util :refer [at]]))

(defn page "Display a codox page"
  []
  (let [html @(rf/subscribe [:docs/page-html])]
   [:div.docs
     (if html
      [:div.codox
      {:ref (fn [el]
              (doseq [el (.querySelectorAll js/document ".codox a")
                      :let [is-github? (string/index-of (.-href el) "github")]]
                (set! (.-href el) ; XXX gotta keep it from doing this to github links!
                      (str (when-not is-github?
                             "/docs/codox/")
                           (if-not is-github?
                             (-> (.-href el)
                               (string/replace  #"http(s)?://.*/" "")
                               (string/replace  #"\.html" ""))
                             (.-href el)))))
              (rf/dispatch [:run-highlighter! el]))
       :dangerouslySetInnerHTML {:__html html}}]
      [ui/loading-spinner (not html) :massive])]))
