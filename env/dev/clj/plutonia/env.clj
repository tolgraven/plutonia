(ns plutonia.env
  (:require
    [clojure.tools.logging :as log]
    [plutonia.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[plutonia started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[plutonia has shut down successfully]=-"))
   :middleware wrap-dev})
