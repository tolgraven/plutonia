(ns plutonia.db
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.reader]))

(def data ; default db. Needs to be cleaned out of content already haha.
  {:state {:menu false
           :is-loading {}
           :theme-force-dark true
           :is-personal false
           :experiments :parallax
           :debug {:layers false
                   :divs false}
           ; :transition :out ;later when proper boot sequence, trigger in on load complete
           }
   :routes {:home "/"
            :about "/about"
            :docs "docs"
            :blog "/blog"
            :log "log"}
   :content {:document {:title "plutonia"
                        :description "plutonia"
                        :base-url "plutonia.club"
                        :author "Joen Tolgraven"
                        :email "plutonia@tolgraven.se"}
             :header {:text ["plutonia" ["border" "land"]]
                      :text-personal ["plutonia" ["club" "venue"]]
                      :menu {:work  [["Services"  "services"  :services] ; should have two sections either by collapse
                                     ["Story"     "about"        :about]         ; or just opp sides with the /
                                     ["Work with us"   "hire"         :hire]
                                     ["Schedule"        "cv"            :cv]]
                             :personal [["Blog"      "/blog"     :blog]
                                        ["Docs"      "/docs"     :docs]
                                        ["Test"      "/test"     :test]
                                        ["Log"       "/log"      :log]]} }
             :intro {:title ["PLU"
                             "TON"
                             "IA"]
                     :text "An experiment within the experiment.
                            In music, lights, and dance."
                     :buttons  [["See set times"        "/cv"]
                                ["Contact us"         [:state [:contact-form :show?] true]]
                                ["Read the news"   "/blog"]]
                     :bg [{:src "img/1.heic.jpg" :alt "Our venue"}
                          {:src "img/2.heic.jpg" :alt "Closer"}
                          {:src "img/3.heic.jpg" :alt "Even closer"}]
                     :logo-bg "img/plutonia-logo-small.heic.jpg"} ; no :src cause goes in :style background-image...

             :services  {:categories
                         [["Music"    "bullhorn"   ["DJs all night long" "Live performances"]]
                          ["Lights"    "lightbulb" ["Some crazy shit" "For reals" "Multiple VJs" "Custom software"]]
                          ["Venue"     "joint"      ["Takeovers" "Workshops" "And more"]] ]
                         :bg {:src "img/2.heic.jpg" :alt "plutonia club venue"}
                         :caption "club"}

             :moneyshot {:title "CO-CREATION"
                         :caption "Well, this could be you"
                         :bg {:src "img/fire.heic.jpg"}}

             :story {:heading {:title "Our story"
                               :bg {:src "img/4.heic.jpg"}}
                     :title "It started with a dream"
                     :text "Plutonia started as an idea in 2021, and was realized at Borderland 2022.
                            The old machine hall transformed into a serious club.
                            For 2023 we already have the hard parts done, and will continue the project, building upon what we already have.
                            And so it will go - each year new projects within the project, experiments within the experiment.
                            Who we are is unimportant, we welcome you whether as a guest or member.
                            Anyways, yada yada yada, vote for our Dreams and give us money yo.
                            More copy more copy more copy lorem ipsum yada heyo swag swag swag banana and maybe let's have chatgpt fill this in for now idunno what to wriiiite shit man. Could I BE any more uninspired? Unclear.
                            More copy more copy more copy lorem ipsum yada heyo. More copy more copy more copy lorem ipsum yada heyo. More copy more copy more copy lorem ipsum yada heyo swag swag swag banana and maybe let's have chatgpt fill this in for now idunno what to wriiiite shit man. Could I BE any more uninspired? Unclear."

                     :images [["Borderland" {:src "img/logo/Borderland-logo-2019.png" :alt "Borderland logo"}
                               "Our overlords."]
                              ["Plutonia" {:src "img/plutonia-logo-small.heic.jpg" :alt "plutonia logo"}
                               "Plutonic laser"]
                              ["Control panel" {:src "img/plutonia-control-panel.png" :alt "plutonia control panel"}
                               "Plutonic control panel"]]}

             :interlude [{:title "What we do"
                          :caption "Mad donkadonk"
                          :bg [:video.media.media-as-bg
                               {:poster "media/snutt-poster.jpg" :src "media/snutt-small.mov" :playsInline true :autoPlay true :muted true } ]}
                         {:title "What you do"
                          :caption "...with it"
                          :bg [:img.media.media-as-bg
                               {:src "img/dancefloor-laser.heic.jpg" }]}
                         {:title "Portfolio forthcoming, have some components"
                          :caption "Be ready"
                          :bg [:img.media.media-as-bg {:src "img/collage-strips.jpg"}]}]
             :cv        {:heading {:title "Lineup and set times"
                                   :target :cv
                                   :bg {:src "img/4.heic.jpg"}
                                   :tint "fg-6"}
                         :title "CV"
                         :caption "Our lineup"
                         :cv {:intro "Subject to change at any time"
                              :background-color-indexes [2 3 5 6 4 1]
                              :timeline
                              [{:category :monday
                                :icon "fas fa-solid fa-graduation-cap"
                                :height-ratio 1
                                :things 
                                [{:from 2004, :to 2007, :what "Natural Science program, mathematics and computer science", :how ["3.96 (19.8) GPA"], :where "Kärrtorps gymnasium" :logo "img/logo/karrtorp-logo.jpg" :color "var(--purple)"}
                                 {:from 2007, :to 2009, :what "Industrial Economics and Management", :how [], :where "Royal Institute of Technology" :logo "img/logo/KTH-logo.jpg" :color "var(--blue-2)"}
                                 {:from 2012, :to 2014, :what "Musicology (60hp)", :how [], :where "Stockholm University" :logo "img/logo/su-logo.png" :color "var(--yellow)"}
                                 {:from 2019, :to 2020, :what "Full stack web development", :how [], :where "Chas Academy" :logo "img/logo/chas-logo.png" :color "var(--green)"}
                                 {:from 2020, :to 2023 , :what "Bachelor programme, Computer Science", :how ["remote"]  :where "Stockholm University" :logo "img/logo/su-logo.png" :color "var(--yellow)"}]}
                               {:category :work
                                :icon "fas fa-solid fa-briefcase"
                                :height-ratio 5
                                :things 
                                [{:from 2001  :to 2005  :what "SvFF"  :position "Referee", :how ["From 5, 7 to 11 (adult) games"] :color "var(--green-3)"  :logo "img/logo/svff-logo.png" :level 0}
                                 {:from 2007, :to 2010, :what "TV4 Sport", :position "PA & Live graphics operator" :how ["Team leader and responsible for group of 7-10 people in live graphics department.
                                                                                                                          ", "Specified, ordered and followed up implementation, rollout and operations for projects, many of them TV graphics solutions, internally vis a vis project leads as well as externally with solutions providers like ChyronHego, Ericsson Broadcast Systems and buyers (for example Svenska Spel).
                                                                                                                              ", "On-site responsibility for testing of systems, communication with providers and operators, working with them on solving issues (or often debugging them myself), reporting and follow-up. Included server administration, code-level debugging and fixes..
                                                                                                                                  ", "Throughout the years I also worked on things like the ice hockey World Championship, 
                                                                                                                                      Fotbollskanalen Europa, Club Calcio and TV4Nyheterna in a variety of roles."], :where "Stockholm" :logo "img/logo/TV4Sport-logo.png" :color "var(--orange)" :level 1}
                                 {:from 2007, :to 2018, :what "TV4", :position "Many different positions, expand for details", :how ["Mainly assistant editor and video editor but on project basis also editor, 
                                                                                                                                      producer, reporter, PA, graphics technician, music administrator etc"] :where "Stockholm" :logo "img/logo/TV4-logo.svg" :color "var(--red-2)" :level 0}
                                 {:from 2008  :to 2011  :what "Royal Institute of Technology" :position "Programming lab/teaching assistant" :how ["Python, C++, Java"] :logo "img/logo/KTH-logo.jpg" :color "var(--blue-2)" :level 2}
                                 {:from 2010, :to 2018, :what "TV4 Sport", :position "Junior producer & live graphics team leader" :how ["Live graphics team leader", "Junior producer", ""], :where "Stockholm" :logo "img/logo/TV4Sport-logo.png" :color "var(--orange)" :level 1}
                                 {:from 2012, :to 2016, :what "A number of clients", :position "Freelance" :how ["SvT", "Endemol", "Svenska Spel",  "Nyhetsbolaget" "C More" "Ericsson Broadcast Systems", "2AM", "Oh my!", "and more"], :where "Stockholm"  :color "var(--fg-1)" :level 2}
                                 {:from 2014, :to 2018, :what "TV4 Sport", :position "Developer" :how ["Developed custom creative tech solutions, generally as a response to IT department or tech suppliers saying a requested feature or workflow wasn’t possible on our tech stack.
                                                                                                        “The missing one-man in-house development team”.", "Personally developed and operated systems during large TV events (such as the football World Cup) to eg. record 2-10+ different channels of international TV and radio, keep all streams syncronized to each other as well as our own feeds, and using this footage and audio live. A combination of network administration, scripting, programming, audio tech and video editing.
                                                                                                                                                                ", ""], :where "Stockholm" :logo "img/logo/TV4Sport-logo.png" :color "var(--orange)" :level 3}
                                 {:from 2013, :to 2022, :what "Club/party promoter", :position "Promoter" :how ["Rave promoter", "Venue operation"], :color "var(--fg-1)" :level 4},
                                 {:from 2018, :to 2019, :what "Systim Nostra", :position "Manager" :how ["Soundsystem operator", "Venue operation"], :where "Kampala" :color "var(--fg-1)" :level 0},
                                 {:from 2019, :to nil,  :what "plutonia audiovisual", :position "Owner-operator, Web developer" :how ["Owner-operator", "Web designer & developer"], :where "Stockholm" :logo "img/tolgrav-square.png" :color "var(--fg-1)" :level 0}
                                 {:from 2021, :to nil,  :what "Crosstown", :how [], :position "Cyclist" :where "Stockholm" :logo "img/logo/crosstown-logo.png" :color "var(--orange-2)" :level 1}
                                 {:from 2022, :to nil, :what "Opter AB", :position "Systems developer (primarily apps)" :how ["Full-time",  "Currently working on a Xamarin Forms iOS (and soon to be Android) app"],  :where "Stockholm" :logo "img/logo/opter-logo.png" :color "hsl(159, 41%, 41%)" :level 2}]}
                               {:category :life
                                :icon "fas fa-heart"
                                :height-ratio 3
                                :things 
                                [{:from 1988  :to 1988  :what "Born"}
                                 {:from 1994  :to 2001  :what "Football Player"}
                                 {:from 2003  :to 2006  :what "Started producing music" :color "var(--purple-2)" :level 0}
                                 {:from 2004  :to 2007  :what "First started coding, in C++ and OpenGL" :color "var(--aqua-3)" :level 1}
                                 {:from 2009  :to 2011  :what "Lived in Australia" :color "var(--green-3)" :level 2}
                                 {:from 2012  :to 2015  :what "Got serious about music production" :color "var(--purple-2)" :level 0}
                                 {:from 2013  :to 2015  :what "Started coding again, with goal of making it a career" :color "var(--aqua-3)" :level 1}
                                 {:from 2016  :to 2018  :what "Started doing functional programming" :color "var(--aqua-3)" :level 1}
                                 {:from 2019  :to 2021  :what "Started focusing on web development" :color "var(--aqua-3)" :level 1}
                                 {:from 2018  :to 2019  :what "Lived in Uganda" :color "var(--green-3)" :level 2}]}]
                              :skills {:software ["Full stack web development and design"
                                                  "Specialized in front-end work: JavaScript (React/Native), CSS"
                                                  "Functional programming, Clojure/ClojureScript (Reagent/Re-frame)"
                                                  "Python, Java, C++"
                                                  "3D and 2D graphics"
                                                  "Git, Docker, Heroku, Firebase, GraphQL, AWS etc"
                                                  "Bash, zsh, fish (20k+ loc), Powershell"
                                                  "Vim ninja"
                                                  "Server admin and some DevOps"]
                                       :digital ["Adobe Premiere, Final Cut Pro X",
                                                 "Adobe Photoshop, After Effects, Illustrator"
                                                 "Ableton Live, Logic Pro, iZotope RX, Adobe Audition"
                                                 "Max/MSP, TouchDesigner, Resolume"
                                                 "Microsoft Excel / Google Sheets - advanced scripting"
                                                 "OS - macOS/Darwin Unix, Linux, Windows including Windows Server"]
                                       :general ["Project management"
                                                 "Working with internal and external stakeholders and ensuring projects stays on track"
                                                 "Interop between technical and artistic or executive"
                                                 "Research"
                                                 "Documenting best practices to ensure stability and ease onboarding"]
                                       :language ["Swedish - Native"
                                                  "English - Fluent, native level"
                                                  "Spanish - Limited, but get by in Spanish speaking countries"]}}}
             :soundcloud {:url "https://soundcloud.com/"
                          :artist "tolgraven"
                          :tunes ["pop-music-for-cool-people-sketch-1-session-1"
                                  "stateless-nearing-completion-messy-mix"
                                  "a-taste-of-what-i-will-sound-like-live"]}

             :gallery [{:src "img/joen-mixer.jpg" :alt "My actual first web project"}
                       {:src "img/live-session-small.jpg" :alt "Ableton Live"}
                       {:src "img/ssiri-balcony-small.jpg" :alt "Some nice people"}
                       {:src "img/video-editing-small.jpg" :alt "Television"}
                       {:src "img/afterglow-new-web-old-small.jpg" :alt "My actual first web project"}]

             :blog    {:heading {:title "News from plutonia crew"
                                 :target :blog
                                 :bg {:src "img/4.heic.jpg"}
                                 :tint "bg-5"}}
             :docs    {:heading {:title "Documentation"
                                 :bg {:src "img/4.heic.jpg"}}}
             :common  {:banner-heading {:bg {:src "img/4.heic.jpg"}}
                       :user-avatar-fallback "img/plutonia-logo-small.heic.jpg"}

             :footer [{:id "left"
                       :email "plutonia@tolgraven.se"
                       :text [(str "© 2023-" (.getFullYear (js/Date.)))]
                       :logo {:src "img/plutonia-logo-small.heic.jpg" :alt "plutonia logo"}}
                      {:id "right"
                       :links [{:name "Github" :href "https://github.com/tolgraven" :icon "github"}
                               {:name "Instagram" :href "https://instagram.com/plutonia" :icon "instagram"}
                               {:name "Facebook" :href "https://facebook.com/plutonia" :icon "facebook"}
                               {:name "Soundcloud" :href "https://soundcloud.com/tolgraven" :icon "soundcloud"}]}]
             :post-footer [{:id "left"
                            :title "Inquiries"
                            :text ["Contact us and let's discuss your idea!"
                                   "Whether you want to join the crew,
                                    have a project in mind
                                    or just want to say hi."]}
                           {:id "right"
                            :title "Links"
                            :links [{:name "Instagram" :href "https://instagram.com/plutonia" :info "plutonia"}
                                    {:name "Facebook" :href "https://facebook.com/plutonia" :info "plutonia"}
                                    {:name "Soundcloud" :href "https://soundcloud.com/tolgraven" :info "tolgraven"}]}
                           {:id "third"
                            :title "Website (and some of the lights)"
                            :text ["by tolgrAVen audiovisual"
                                   [:a {:href "https://tolgraven.se"}
                                    "tolgraven.se"]]
                            :img [{:src "img/logo/Borderland-logo-2019.png" :alt "borderland logo"}
                                  {:src "img/logo/icon/react-logo-icon.png" :alt "react logo"}]}]}

   :options {:auto-save-vars true
             :transition {:time 500 :style :slide} ; etc
             :blog {:posts-per-page 3} ; XXX should go in query-params no
             :theme {:dark-mode true
                     :colorscheme "default"}
             :firebase {:project :main
                        :settings {}}
             :github {:user "plutonia"
                      :repo "plutonia"}
             :hud {:timeout 30 :level :info}}
   :cookie-notice {:a :map}})


(rf/reg-event-db :init-db
  (fn [db _]
    data))
