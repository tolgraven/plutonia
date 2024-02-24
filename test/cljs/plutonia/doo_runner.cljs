(ns plutonia.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [plutonia.core-test]))

(doo-tests 'plutonia.core-test)

