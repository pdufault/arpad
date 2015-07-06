(ns arpad.pool.manager
  (:require [clojure.core.async :as async :refer [<! >! go-loop]]
            [clojure.core.match           :refer [match]]
            [arpad.pool                   :refer [lookup-player
                                                  update-pool
                                                  standings]]))

(defn- mutate-pool
  "Generate a new pool, depending on the contents of the message"
  [pool msg]
  (match [msg]
    [{:new-game [player-a player-b score]}]
    (-> pool
        (update-pool player-a player-b score)
        (update-in [:players (:id player-a) :total-games] inc)
        (update-in [:players (:id player-b) :total-games] inc))

    :else pool))

(defn- gen-report
  "Generate a report based on the contents of the message"
  [pool msg]
  (match [msg]
    [{:standings n}]
    (if n (standings pool n) (standings pool))

    [{:new-game [player-a player-b _]}]
    (lookup-player pool player-a player-b)

    :else nil))

(defn spawn-pool-manager
  [init-pool in-chan out-chans]
  {:pre [(contains? out-chans :player-report)]}
  (go-loop [msg (<! in-chan)
            pool init-pool]
    (when msg
      (let [pool' (mutate-pool pool msg)
            report (gen-report pool' msg)]
        (when report
          (>! (:player-report out-chans) report))
        (recur (<! in-chan) pool')))))
