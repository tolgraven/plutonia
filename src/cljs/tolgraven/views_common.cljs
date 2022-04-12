(ns tolgraven.views-common
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [clojure.string :as string]
   [markdown.core :refer [md->html]]
   [tolgraven.ui :as ui]
   [tolgraven.db :as db]
   [tolgraven.util :as util :refer [at]]))

(defn flashing-ersatz-text-like-everyone-uses
  "Better than wee loading spinner no? Eg Docs, we know big page is coming
   so while loading should be expanded to that size already yo"
  [row-count])

;; TODO curr 1px gap between outer lines and img. Fix whatever causing this by mistake (think lines are half-width)
;; BUT also retain (and try 2px?) bc looks rather nice actually
(defn header-logo [[text subtitle]]
  [:div.header-logo
   [:a {:href @(rf/subscribe [:href :home])} ;works w/o reitit fiddle
    [:h1 text]]
   [:div.header-logo-text ; if I want this to do its flip when changes text, do I need to make a whole componentDidChange dance with it? :I
    (for [line subtitle] ^{:key (str "header-text-" line)}
      [:p line])]])

(defn header-nav "PLAN: / across with personal stuf on other side. Fade between logos depending on mouse hover..."
  [sections]
  (let [put-links (fn [links]
                    (doall
                     (for [[title url page] links
                           :let [id (str "menu-link-" (string/lower-case title))]]  ^{:key id}
                          [:li [:a {:href @(rf/subscribe [:href page])
                                    :name title :id id
                                    :class (when (= page
                                                    @(rf/subscribe [:common/page-id]))
                                             "is-active")}
                                (string/upper-case title)]])))]
    [:menu ; XXX put bg stuff in mwnu not nav...
     [:nav
      [:div.nav-section
       [:ul.nav-links
        (put-links (:work sections))]]

      [:div.nav-section
       [:ul.nav-links
        {:style {:position :absolute, :right 0, :top 0}}
        (put-links (:personal sections))]]]

     [:div.menu-toggles
      {:style {:position "absolute" :right 0 :bottom 0} }
      ; [input-toggle "theme-force-dark" [:state [:theme-force-dark]]]
      ; [:label.show-in-menu {:for "theme-force-dark" :class "theme-label"} "Theme"]
      ; [ui/toggle [:state [:debug-layers]]]
      ; [input-toggle "debug-layers" [:debug [:layers]]]
      ; [:label.show-in-menu {:for "debug-layers"} "Debug"]
      ] ]))


(defn header [{:keys [text text-personal menu]}] ; [& {:keys [text menu]}] ; wtf since when does this not work? not that these are optional anyways but...
  [:<>
   [ui/input-toggle "nav-menu-open" [:menu] :class "burger-check"]
   [:header
    {:class (when @(rf/subscribe [:state [:hidden-header-footer]])
              "hide")}
    [:div.cover.cover-clip] ;covers around lines and that... XXX breaks when very wide tho.
    [header-logo @(rf/subscribe [:header-text])]
    [header-nav menu]

    [ui/loading-spinner (rf/subscribe [:loading]) :still]
    [:a {:href @(rf/subscribe [:href :blog])}
     [:button.blog-link-btn.noborder
      [:i.fa.fa-pen-fancy]]]
    [ui/user-btn]
    [:label.burger {:for "nav-menu-open"}]]

   [:div.fill-side-top
    {:class (when-not @(rf/subscribe [:state [:scroll :past-top]])
              "hide")}]
   
   [:div.line.line-header
    {:class (when @(rf/subscribe [:state [:hidden-header-footer]])
             "hide")}]])


(defn post-footer "Extra stuff after the basic footer. Not very useful for me but for other sites."
  [content]
  [:div.footer-content.post-footer-content ;; XXX should adapt to available height, also disappear...
    (for [{:keys [title text id links img] :as column} content
          :let [id (str "post-footer-" id)]] ^{:key id}
         [:div.footer-column {:class (:id column)
                              :id id}

          (when title [:h4 title])
          (when text (for [line text] ^{:key (str id "-" line)}
                          [:h5 line]))
          (when links [:div.footer-links
                       (for [{:keys [name href info]} links] ^{:key (str "post-footer-link-" name)}
                            [:a {:href href :name name}
                             [:div.footer-link-with-text
                              [:p name] [:p info]]])])
          (when img (for [img img]  ^{:key (str id "-" (:src img))}
                      [:img.img-icon img]))])])

(defn footer-sticky "Thinking just something tiny visible maybe not actually entire time but at whatever points, framing things in. Might be job for css tho dunno"
  [content]
  [:<>
    [:footer.footer-sticky>div.footer-content
      {:style {:position :fixed}}
      [:div.line.line-footer] ;cant this be outside main ugh
        (for [{:keys [title text id links] :as column} content
          :let [id (str "footer-stick-" id)]] ^{:key id}
          [:div.footer-column {:id id}
          [:h3 title]])]])

(defn footer "Might want to bail on left/middle/right just push whatever. do the current ids matter?"
  [content]
  [:footer.footer-sticky ; [:footer>div.footer-content
   {:class (string/join " "
                        [(when @(rf/subscribe [:state [:hidden-header-footer]])
                           "hide")
                         (when @(rf/subscribe [:state [:scroll :at-bottom]])
                           "full")])
    :style (when-not @(rf/subscribe [:state [:hidden-header-footer]])
             {:max-height @(rf/subscribe [:get-css-var "footer-height-current"])})}
   
   [:div.line.line-footer] ;cant this be outside main ugh
   [:div.footer-content ;; XXX should adapt to available height, also disappear...
    (for [{:keys [title text id links logo] :as column} content
          :let [id (str "footer-" id)]] ^{:key id}
         [:div.footer-column {:id id}

          (when logo
            [:img.img-icon logo])
          [:div
           (when title [:h4 title])
           (when text (for [line text] ^{:key (str id "-" line)}
                        [:h5 line]))]
          (when links [:div.footer-icons
                       (for [{:keys [name href icon]} links] ^{:key (str "footer-link-" name)}
                            [:a {:href href :name name}
                             [:i.fab {:class (str "fa-" icon)}]])])])]
   [post-footer @(rf/subscribe [:content [:post-footer]])]])


(defn to-top "A silly arrow, and twice lol. why." [icon]
 (let [icon (or icon "angle-double-up")
       i [:i {:class (str "fas fa-" icon)}]]
    [:a {:id "to-top" :class "to-top" :href @(rf/subscribe [:href "#main"]) :name "Up"} i]))

