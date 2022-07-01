(ns tolgraven.search.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [clojure.string :as string]
   [tolgraven.ui :as ui]
   [tolgraven.blog.views :as blog]
   [tolgraven.util :as util :refer [at]]))

(defn button "Search button, press to show input field..."
  []
  (let [open? (rf/subscribe [:search/open?])]
    [:button.search-ui-btn.noborder.nomargin
     {:on-click (fn [e]
                  (rf/dispatch [:search/state [:open?]
                                (not @open?)])
                  (when-not @open? ; going from closed to open
                    (rf/dispatch [:focus-element "search-input"])))}
     [:img {:src "svg/search-ico.svg"
            :style {:width "1.2em" :height "1.2em"
                    :filter "invert(100%)"}}]]))

(defn input "Search input field"
 [collections & {:as args :keys [model opts query-by open?]}]
 (let [external-model (r/atom model)
       internal-model (r/atom (or @model ""))
       div-ref (r/atom nil)
       last-timeout-id (atom nil)]
   (fn [collections &
        {:keys [query-by model on-enter placeholder width height open?]
         :or {height "2em"
              query-by ["text" "title"]}}]
     (let [latest-ext-model @model
           suggestion (first @(rf/subscribe [:search/autocomplete-for-current-query (first collections)]))]
       (when (not= @external-model latest-ext-model)
         (reset! external-model latest-ext-model)
         (reset! internal-model latest-ext-model))
   [:div.search-input-container
        
      [:input#search-input.search-input ;problem if multiple search boxes on same page tho
      {:type "search"
       :style {:min-width width :max-width width
               :min-height height :max-height height
               :padding (when (or (zero? width) (zero? height)) 0)
               :border (when (or (zero? width) (zero? height)) 0)}
       :placeholder placeholder
       :autoComplete "off"
       :value       @internal-model
       :ref         #(when % (reset! div-ref %))
       :on-change (fn [e] ; XXX needs debounce I guess
                    (let [new-val (-> e .-target .-value)]
                      (reset! internal-model new-val)
                      (reset! external-model @internal-model)
                      ; (js/clearTimeout @last-timeout-id)
                      #_(reset! last-timeout-id
                              (js/setTimeout #(doseq [coll collections]
                                               (rf/dispatch [:search/search coll new-val query-by opts]))
                                             250))
                      (doseq [coll collections]
                                (rf/dispatch [:search/search coll new-val query-by opts]))))
       :on-key-up (fn [e]
                    (case (.-key e)
                      "Enter" (when (and on-enter (not= "" @model))
                                (reset! internal-model suggestion)
                                (reset! external-model @internal-model))
                      "Escape" (do (.preventDefault e) ; can't seem to stop it from blanking query hmm
                                   (rf/dispatch [:search/state [:open?] false]))
                      true))}]
      [:div.search-input.search-input-autocomplete.form-control
          {:style {:min-height height
                   }}
         (:text suggestion)
         ]])))) ;after not before, want to be able to override stuff duh

(defn suggestions "Display a dropdown of suggested further terms"
  [collection]
  (let [suggestions (rf/subscribe [:search/autocomplete-for-current-query collection])
        last-suggestions (atom nil)]
    (fn [collection]
      (let [suggestions' (or @suggestions @last-suggestions)]
        (reset! last-suggestions suggestions')
        [:div.search-autocomplete
         {:class (when (and @(rf/subscribe [:search/open?])
                            (not (string/blank? @(rf/subscribe [:search/get-query "blog-posts"]))))
                   "search-autocomplete-open")}
         (for [suggestion (drop 1 suggestions') ; rework as map-indexed so can highhlight and pick by keyboard
               :let [html (:html suggestion)
                     text (:text suggestion)]]
           [:div.search-autocomplete-item
            {:on-click (fn [e]
                         (rf/dispatch [:search/search collection text ["text" "title"]]))}
            [ui/md->div html]])]))))


(defn instant-result-category "Wrapper for type of results/collection"
  [collection component]
  (if-let [hits (:hits @(rf/subscribe [:search/results-for-query collection]))]
    [component hits]
    [ui/loading-spinner true]))


(defn blog-post-results "Show hits that are blog posts"
  [hits]
  [:div.blog
   (doall
   (for [hit hits
         :let [{:keys [highlights document]} hit
               {:keys [id permalink title text user ts]} document]]
     ^{:key (str "search-result-post-" id)}
     [ui/appear-merge "zoom-y fast"
      [:div.search-instant-result.blog-post
       [:div.blog-post-header-main
        [:a {:href @(rf/subscribe [:blog/permalink-for-path (or permalink id)])}
         [:h2.blog-post-title title]]
        [blog/posted-by id user ts]
        [blog/tags-list document]]
       (for [highlight highlights]
         ^{:key (str "search-result-highlight-" (:snippet highlight))}
         [ui/md->div (:snippet highlight)])]]))])

(defn blog-comment-results "Show hits that are blog post comments"
  [hits]
  (when (seq hits)
    [:div.blog-comments>div.blog-comments-inner
     (doall
      (for [hit hits
            :let [{:keys [highlights document]} hit
                  {:keys [id title text user ts]} document]]
        ^{:key (str "search-result-comment-" id)}
        [ui/appear-merge "zoom fast"
         [:div.search-instant-result.blog-comment-around.flex
          [:div.blog-comment-border]
          [:section.blog-comment
           [ui/user-avatar @(rf/subscribe [:user/user user])]
           [:div.blog-comment-main
            [:h4.blog-comment-title title]
            [blog/posted-by id user ts] 
            (for [highlight highlights]
              ^{:key (str "search-result-highlight-" (:snippet highlight))}
              [:div.blog-comment-text
               [ui/md->div (:snippet highlight)]])]]]]))]))

(defn instant-results "Show results while searching"
  [open?]
  (let []
    (when (and open?
               (not (string/blank? @(rf/subscribe [:search/get-query "blog-posts"]))))
      [:section.search-instant-results
       [instant-result-category "blog-posts" blog-post-results]
       [instant-result-category "blog-comments" blog-comment-results]])))

(defn full-results "More full complete and whatnot"
  [collection query])

(defn ui "The search ui. Initially runs over blog-posts and comments, but should later also search docs and hence source-code."
  [collection]
  (let [open? (rf/subscribe [:search/open?])
        results-open? (rf/subscribe [:search/results-open?])]
    (fn [collection]
      [:section.search-ui
       {:ref #(when % (rf/dispatch [:search/init]))}
       
       [input ["blog-posts" "blog-comments"]
        :model (rf/subscribe [:search/get-query "blog-posts"])
        :open? @open?
        :placeholder "Search"
        :height (if @open? "2em" 0)]
       [suggestions "blog-posts"]
       ; [suggestions "blog-comments"]
       [instant-results @open?]
       ])))
