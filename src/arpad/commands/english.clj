(ns arpad.commands.english
  (:require [arpad.release-notes :refer :all]
            [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [instaparse.core :as insta]))

(def parser
  (insta/parser (io/resource "english.bnf")))

(defn str->Command
  [string]
  (match [(insta/parse parser string)]
    [[:Command [:NewGame [:Player a] [:Player b]]]]
    {:new-game [{:id (keyword a)} {:id (keyword b)} 1]}

    [[:Command [:Follow [:Player p]]]]
    {:follow {:id (keyword p)}}

    [[:Command [:Ignore [:Player p]]]]
    {:ignore {:id (keyword p)}}

    [[:Command [:Standings]]]
    {:standings nil}

    [[:Command [:Standings n]]]
    {:standings (Integer/parseInt n)}

    [[:Command [:Rating [:Player p]]]]
    {:rating {:id (keyword p)}}

    [[:Command [:ReleaseNotes]]]
    {:release-notes alpha-0-0-2}

    :else
    {:error string}))
