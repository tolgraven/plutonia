(ns plutonia.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [pjstadig.humane-test-output]
            [reagent.core :as reagent :refer [atom]]
            [plutonia.core :as tc]))

(deftest test-home
  (is (= true true)))
