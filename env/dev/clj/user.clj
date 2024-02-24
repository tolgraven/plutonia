(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [plutonia.config :refer [env]]
    [clojure.pprint]
    [clojure.spec.alpha :as s]
    [clojure.tools.logging :as log]
    [expound.alpha :as expound]
    [mount.core :as mount]
    [plutonia.figwheel :refer [start-fw stop-fw cljs]]
    [plutonia.handler :as handler]
    [plutonia.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint)) ; (tap> ) goes to repl, anything else?

(defn start "Starts application.  You'll usually want to run this on startup." []
  (doseq [component (-> (mount/start-without #'plutonia.core/repl-server)
                        :started)]
    (log/info component "started"))
  (start-fw))

(defn stop "Stops application." []
  (doseq [component (-> (mount/stop-except #'plutonia.core/repl-server)
                        :stopped)]
    (log/info component "stopped")))

(defn restart "Restarts application." []
  (stop)
  (start))

(defn reload-deps []
  (log/warn "Disabled")
  #_(require 'alembic.still)
  #_(alembic.still/load-project))

(defn restart-handler []
  (mount/stop #'handler/app-routes)
  (mount/start #'handler/app-routes))
; (restart-handler)
