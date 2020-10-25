(ns tolgraven.core
  (:require
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as gevents]
    [goog.object :as gobj]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [cljs-time.core :as ct]
    [cljs-time.format :refer [formatters formatter unparse]]
    [cljsjs.smoothscroll-polyfill :as smooth]
    [react-highlight.js :as highlight]
    [cljsjs.highlight :as hljs]

    [tolgraven.ajax :as ajax]
    [tolgraven.events]
    [tolgraven.db :as db]
    [tolgraven.subs]
    [tolgraven.ui :as ui]
    [tolgraven.util :as util]
    [tolgraven.views :as view]
    [tolgraven.views-common :as common]
    [tolgraven.blog.views :as blog]
    [tolgraven.user.views :as user]
    [tolgraven.experiments :as experiment]
    [reitit.core :as reitit]
    [reitit.frontend.history :as rfh]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [clojure.pprint :refer [pprint]])
  (:import goog.History))

(defn safe "Error boundary for components. Also prints/logs error"
  [category component]
 (let [exception (rf/subscribe [:state [:exception category]])] ;or reg not ratom or would cause etra re-render?
  (r/create-class
  {:display-name (str "Boundary: " (name category))
   :component-did-catch (fn [this error info] ;this should log instantly tho
                          ; (reset! exception {:error error :info info}) ; error and info are empty.
                          (rf/dispatch [:state [:exception category] {:error error :info info}])) ; error and info are empty.
    ; :get-derived-state-from-error ;"defined as instance method and will be ignored. define as static"
   ; (fn [error] ;this should update state to serve like, an error page (for render) "if using getDerivedState methods, the state has to be plain JS object as React implementation uses Object.assign to merge partial state into the current state."
   ;  (clj->js ;cant get it working. but is supposed to used this, console warns...
   ;   [:div.component-failed
   ;    [:p "Component exception:"]
   ;    [:pre (str "Error: " (:error exception)
   ;               "\nInfo: " (:info exception))]
   ;    [:div "Click to attempt reload"
   ;     [:button {:on-click #(reset! exception nil)}]]]))
   :reagent-render
   (fn [category component]
    (if-not @exception   ;state change downstream? then it gets easier to debug "in-page",
     component
     (let [[component state] component] ;cant remember why this is
       ; (println "Error")
       ; (util/log :error "Component" (string/join " " ((js->clj component) state))) ;replace with better logging eh...
        [:section.component-failed
          [:p "Component exception"]
          (when (string? (:error @exception))
            [:pre (:error @exception)])
          (when (object? (:info @exception))
            [:pre (-> @exception :info js->clj :componentStack)])

          [:div
           [:button {:on-click #(rf/dispatch [:state [:exception category] nil])}
            "Attempt reload"]]])))})))


(defn page "Render active page inbetween header, footer and general stuff." []
  [:<>
   [safe :header [common/header @(rf/subscribe [:content [:header]])]] ;TODO smooth transition to personal
   [:a {:name "linktotop" :id "linktotop"}]
   
   [safe :user [user/user-section @(rf/subscribe [:user/active-section])]]

   (if-let [page @(rf/subscribe [:common/page])]
     [:main.main-content.perspective-top
      {:class (if @(rf/subscribe [:state [:transition]])
                "hidden"; "slide-in slide-out-left" ; hidden
                "visible")}; "slide-in ")} ; visible
      ; should be: outgoing page put at like :common/last-page, plus a flag
      ; render both, wrapped in div each. outgoing starts middle goes left/right,
      ; incoming starts left/right animates to middle (simultaneously)
      ; finish -> flag unset -> kill/novisible last page div.
      [safe :page [page]]] ; this doesnt reload when switch page. need to do manually...
     ; so will need to track in db not ratom, then reset on nav.
     ; also get-css-var still ticks every 500ms whenever safe triggers/any unhandled errors(?)... wtf?
     [common/loading-spinner true])

   [common/footer @(rf/subscribe [:content [:footer]])]
   [safe :hud [ui/hud (rf/subscribe [:hud])]]
   [common/to-top]
   [:a {:name "bottom"}]])


(defn with-heading
  [heading-path component & [override]]
  [:<>
   [view/fading-bg-heading (merge @(rf/subscribe [:content heading-path])
                                  override)]
   component])

(defn test-page []
  [:<>
   [view/fading-bg-heading (merge @(rf/subscribe [:content [:common :banner-heading]])
                                  {:title "Experiments" :tint "green"})]
   [:section.experiments.solid-bg.fullwide.noborder
    (let [routes {:parallax #'experiment/parallax :codemirror #'experiment/code-mirror}
          tab @(rf/subscribe [:state [:experiments]])]
    [:ul.tabs-container.flex
     (for [tab-key [:parallax :codemirror :broken]] ^{:key tab-key}
            [:li [:button {:class (if (= tab tab-key)
                                    "noborder"
                                    "topborder")
                           :on-click #(rf/dispatch [:state [:experiments] tab-key])
                           } tab-key]])])]])

(defn home-page []
  [view/ui])

(defn doc-page []
  (let [docs @(rf/subscribe [:content [:docs]])]
    [with-heading [:docs :heading]
     [:section.docs.solid-bg.hi-z
      {:ref #(when % (util/run-highlighter! "pre" %))} ; very nice way to get a did-mount
      [ui/md->div (:md docs)]]]))


(defn blog-page []
  [with-heading [:blog :heading]
   [blog/blog]])

(defn post-blog-page [] ; how nicely set is-personal for this but also unset etc yada
  [with-heading [:blog :heading] 
   [blog/post-blog]])


(defn log-page []
  [with-heading [:common :banner-heading]
   [ui/log (rf/subscribe [:option [:log]])
    (rf/subscribe [:get :diagnostics])]
   {:title "Log" :tint "blue"}])


(def router ; XXX weird thing doesnt automatically scroll to top when change page...
  (reitit/router
    [["/"
      {:name        :home
       :view        #'home-page
       :controllers [{:start (fn [_]
                               (rf/dispatch [:state [:is-personal] true])
                               (rf/dispatch [:page/init-home]))
                      :stop (fn [_]
                              (util/log "exit home")
                              (rf/dispatch [:state [:is-personal] true]))}]}]
     ["/docs" {:name :docs
               :view #'doc-page
               :controllers [{:start (fn [_] (doall (map rf/dispatch [[:page/init-docs]
                                                                      [:state [:is-personal] true]])))}]}] ; really overkill this triggers each time. gotta be built-in solution somewhere? else work around
     ["/blog" {:name :blog
               :view #'blog-page
               :controllers [{:start (fn [_] (rf/dispatch [:page/init-blog]))}]}]
     ["#about" {:name :story
               :view #'home-page}]
     ["#link-services" {:name :services
               :view #'home-page}]
     ["/log" {:name :log
              :view #'log-page}]
     ["/test" {:name :test
              :view #'test-page}] ]))

(defn start-router! []
  (rfe/start!
    router
    (fn [match _] (rf/dispatch [:common/start-navigation match]))
    {:ignore-anchor-click? (fn [router e el uri] ;; Add additional check on top of the default checks
                           (and (rfh/ignore-anchor-click? router e el uri)
                                (not= "false" (gobj/get (.-dataset el) "reititHandleClick"))))}))
                                ; (not= "false" (HistoryEventType/get (.-dataset el) "reititHandleClick"))))}))

(def firebase-app-info
  {:apiKey "AIzaSyBsqgWFXZfLq4W8FPRfSVd3AzSt183w9HQ"
   :projectId "tolgraven-8fd35"
   :authDomain "tolgraven-8fd35.firebaseapp.com"
   :databaseURL "https://tolgraven-8fd35.firebaseio.com"
   :storageBucket "tolgraven-8fd35.appspot.com"})
;; -------------------------
;; Initialize app
(defn mount-components "Called each update when developing" []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! "Called only on page load" []
  (start-router!)
  (rf/dispatch-sync [:init-db])
  (ajax/load-interceptors!)

  (firebase/init :firebase-app-info      firebase-app-info ;@(rf/subscribe [:option [:firebase]])
                 :firestore-settings     {:timestampsInSnapshots true} ; See: https://firebase.google.com/docs/reference/js/firebase.firestore.Settings
                 :get-user-sub           [:fb/get-user]
                 :set-user-event         [:fb/set-user]
                 :default-error-handler  [:fb/error])
  (rf/dispatch [:id-counters/fetch])
  ; dispatch firebase general fetches, test case is silly id counter persistence
  ; which then needs to be fetched into app-db once ready
  (util/log "Init complete, mounting root component")
  (mount-components))
