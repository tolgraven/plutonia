(ns plutonia.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[plutonia started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[plutonia has shut down successfully]=-"))
   :middleware identity})
