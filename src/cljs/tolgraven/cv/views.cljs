(ns tolgraven.cv.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [clojure.string :as string]
   [tolgraven.ui :as ui]
   [tolgraven.util :as util :refer [at]]))

(defn box "One thing, accomplishment, employment, etc"
  [{:keys [from to what position how where logo color] :as all} domain pos size overlap-level]
  (let [expanded? (r/atom false)
        closing? (r/atom false)]
    (fn [{:keys [from to what position how where logo color] :as all} domain pos size overlap-level]
      [:div.cv-detail
       {:on-click (fn []
                    (swap! closing? not)
                    (js/setTimeout #(do (swap! expanded? not)
                                        (swap! closing? not))
                                   500))
        :style (merge {:background-color color}
                      (when-not @expanded?
                        {:left pos
                         :top (str (+ 2.5 (* 19 overlap-level)) "%") ;only supports 4 tall then tho
                         :width size}))
        :class (str (when @expanded? "cv-detail-expanded ")
                    (when @closing? "cv-detail-closing"))}
       [:div.cv-bg-logo
        {:style {:background-image (str "url(../../" logo ")")}}]
       [:p.cv-from from]
       [:p.cv-to (if to to "current")]
       [:p.cv-what [:strong what]]
       (if @expanded?
         (for [item how]
           [:p.cv-how [:i.fas.fa-arrow-right] item])
         [:p.cv-position position])
       [:p.cv-where where]
       (when logo
         [:img {:src logo}])])))

(defn capabilities "The various skills"
  [skills]
  (let [topic (fn [id icon]
                [:div.cv-skill
                 {:class (str "cv-" (name id))}
                 [:h3 [:i {:class icon}] (str " " (string/capitalize (name id)))]
                 (for [line (id skills)] ^{:key line}
                   [:p.cv-skill-line "- " [:span line]])])
        software (topic :software "fas fa-code")
        digital (topic :digital "fas fa-calculator")
        general (topic :general "fas fa-globe")
        language (topic :language "fas fa-globe")]
    [:div.cv-skills
     {:style {:min-height "20em"}}
     [:h1 "Skills"]
     [ui/carousel-normal :cv/skills {} [software digital general language]]]))

(defn cv "Main cv component"
  []
  (let [ref-fn (fn [el]
                 (when el
                   (rf/dispatch [:dispatch-in/ms 2000 [:state [:fullscreen :cv] true]])
                   (rf/dispatch [:dispatch-in/ms 3000 [:scroll/by 50]])
                   (rf/dispatch [:dispatch-in/ms 4500 [:scroll/by -37]])
                   (rf/dispatch [:dispatch-in/ms 5500 [:focus-element "fullscreen-btn"]])))]
   (fn []
    (let [{:keys [title caption cv]} @(rf/subscribe [:content [:cv]])
        {:keys [intro education work life skills]} cv
        first-year (apply min (map :from (concat education work)))
        last-year  (apply max (map #(if (number? %)
                                      %
                                      2025)
                                   (map :to (concat education work))))
        get-pos (fn [start end]
                  (str (* 95
                          (/ (- start first-year)
                             (- last-year first-year)))
                       "%"))
        get-size (fn [start end]
                   (let [end (if (number? end) end 2024)]
                     (str (* 93
                             (/ (- end start)
                                (- last-year first-year)))
                          "%")))
        curr-end (atom 1970)
        overlap-level (r/atom 0)
        gen-items (fn [domain]
                    (doall (for [{:keys [from to level] :as all} (domain cv)
                                 :let [last-end @curr-end
                                       new-end (reset! curr-end to)
                                       olevel (if (> last-end from)
                                               (swap! overlap-level inc)
                                               (reset! overlap-level 0))
                                       id (str from "-" to "-" (:what all))]]
                            ^{:key id}
                             [box all domain
                              (get-pos from to)
                              (get-size from to)
                              (or level @overlap-level)])))
        boxes [:div.cv-boxes
               {:ref #(when %
                        (set! (.-scrollLeft %) (.-scrollWidth %)))}
               [ui/close #(rf/dispatch [:state [:fullscreen :cv] false])]
               [:div.cv-items.cv-education
                [:h1 [:i.fas.fa-solid.fa-graduation-cap] "education"]
                (gen-items :education)]
               [:div.cv-items.cv-work
                [:h1 [:i.fas.fa-solid.fa-briefcase] "work"]
                (gen-items :work)]
               [:div.cv-items.cv-life
                [:h1 [:i.fas.fa-book] "life"]
                (gen-items :life)]]
        fullscreen? (when @(rf/subscribe [:fullscreen/get :cv])
                      "fullscreen")
        win-fullscreen? @(rf/subscribe [:state [:window :fullscreen?]])]
    [:section#cv.cv.nopadding.noborder
     {:class fullscreen?
      :ref ref-fn}
     [:div.cv-intro
      [:img.fullwide
       {:src "img/logo/tolgraven-logo.png"}]
      [:p (:intro cv)]
      [:div.center-content
       [:div.cv-howto
        [:p "Click a box for further details."]

        (if-not win-fullscreen?
          "Scroll sideways to look back, or click "
          "Done snooping? Click ")
        [:button#fullscreen-btn
         {:on-click #(rf/dispatch [:window/fullscreen! (not win-fullscreen?)])}
         (if-not win-fullscreen?
           "Fullscreen"
           "Exit fullscreen")]
        (if-not win-fullscreen?
          " or just tap your big ole' <Space>| to maximize your browser window. "
          " to return to your trusty desktop")]]]
     boxes
     [ui/fading :dir "bottom"]
     [capabilities skills]]))))

