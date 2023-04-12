(ns tolgraven.gpt.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [clojure.string :as string]
   [tolgraven.ui :as ui]
   [tolgraven.util :as util]))

(defn gpt-message "A single gpt message"
  [prompt response thread]
  (let [user (rf/subscribe [:user/user (:user thread)])
        hovered? (r/atom false)]
    (fn [prompt response thread]
      [:div.gpt-message
       [:div.gpt-message-text.gpt-message-prompt
        [:span prompt]
        [:span.gpt-message-time
         {:on-mouse-over #(reset! hovered? true)
          :on-mouse-leave #(reset! hovered? false)}
         (util/timestamp (:time thread))
         [:span.gpt-message-time-exact
          (when @hovered? (util/unix->ts (:time thread)))]]
        [:div.gpt-message-user.flex
         (or (-> (:name @user)
                 (string/split #"\s|-|_")
                 (->> (map string/capitalize)
                      (map (partial take 1))
                      flatten
                      (apply str)))
             "anon")
         [ui/user-avatar @user]]]
       [:div.gpt-message-text.gpt-message-reply
        (or response
            "...")] ])))

(defn gpt "A place to hang out with real-time messaging"
  []
  (let [content @(rf/subscribe [:gpt/content])
        latest-id @(rf/subscribe [:gpt/latest-seq-id])]
    [:section.gpt.noborder.covering-2
     [:div.gpt-messages
      {:ref #(when % (set! (.-scrollTop %) (.-scrollHeight %)))}
      (for [message content] ^{:key (str "gpt-message-" (:time message) "-" (:user message))}
        [gpt-message message])]

     [:div.gpt-input.flex 
      [ui/input-text
       :path [:form-field [:gpt]]
       :placeholder "Message"
       :on-enter #(rf/dispatch [:gpt/post latest-id])]
      [:button {:on-click #(rf/dispatch [:gpt/post-in-thread latest-id])}
       [:i.fa.fa-arrow-right]]] ]))

(defn thread
  [id]
  (let [open? (r/atom false)
        thread (rf/subscribe [:gpt/thread id])]
    (fn [id]
      (let [user @(rf/subscribe [:user/user (:user @thread)])]
        [:div.gpt-thread-container
         [:div.flex
          {:style {:align-items "center"}}
          [:span "Thread "]
          [:button.gpt-open-thread
           {:on-click #(swap! open? not)}
           id]

          [:div.gpt-message-user.flex
           (or (:name user) "anon")
           [ui/user-avatar user]]]

         (when @open?
           [:div.gpt-messages.gpt-thread.open
            {:ref #(when % (set! (.-scrollTop %) (.-scrollHeight %)))} 

            (for [[prompt response] (partition 2 2 "..." (:messages @thread))]
              [gpt-message prompt response @thread])

            [:div.gpt-input.flex 
             [ui/input-text
              :path [:form-field [:gpt]]
              :placeholder "Message"
              :on-enter #(rf/dispatch [:gpt/post-in-thread id (:messages @thread)])]
             [:button {:on-click #(rf/dispatch [:gpt/post-in-thread id (:messages @thread)])}
              [:i.fa.fa-arrow-right]]]]) ]))))

(defn threads "A place to hang out with real-time messaging"
  []
  (let [ids @(rf/subscribe [:gpt/thread-ids])
        new-id @(rf/subscribe [:gpt/new-thread-id])]
    [:section.gpt.noborder.covering-2
     [:div.gpt-messages
      {:ref #(when % (set! (.-scrollTop %) (.-scrollHeight %)))}
      (for [id ids] ^{:key (str "gpt-thread-" id)}
        [thread id])]
      [:button
       {:on-click #(rf/dispatch [:gpt/new-thread ids new-id])}
       "New thread"]]))

