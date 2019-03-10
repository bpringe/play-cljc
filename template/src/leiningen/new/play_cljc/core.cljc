(ns {{name}}.{{core-name}}
  (:require [{{name}}.utils :as utils]
            [{{name}}.move :as move]
            [play-cljc.gl.core :as c]
            [play-cljc.gl.entities-2d :as e]
            #?(:clj  [play-cljc.macros-java :refer [gl math transform]]
               :cljs [play-cljc.macros-js :refer-macros [gl math transform]])))

(defonce *state (atom {:mouse-x 0
                       :mouse-y 0
                       :pressed-keys #{}
                       :x-velocity 0
                       :y-velocity 0
                       :player-x 0
                       :player-y 0
                       :can-jump? false
                       :direction :right
                       :player-images {}
                       :player-image-key :walk1}))

(defn init [game]
  ;; allow transparency in images
  (gl game enable (gl game BLEND))
  (gl game blendFunc (gl game SRC_ALPHA) (gl game ONE_MINUS_SRC_ALPHA))
  ;; load images and put them in the state atom
  (doseq [[k path] {:walk1 "player_walk1.png"
                    :walk2 "player_walk2.png"
                    :walk3 "player_walk3.png"}]
    (utils/get-image path
      (fn [{:keys [data width height]}]
        (let [;; create an image entity (a map with info necessary to display it)
              entity (e/->image-entity game data width height)
              ;; compile the shaders so it is ready to render
              entity (c/compile game entity)
              ;; assoc the width and height to we can reference it later
              entity (assoc entity :width width :height height)]
        ;; add it to the state
        (swap! *state update :player-images assoc k entity))))))

(def screen-entity
  {:viewport {:x 0 :y 0 :width 0 :height 0}
   :clear {:color [(/ 173 255) (/ 216 255) (/ 230 255) 1] :depth 1}})

(defn run [game]
  (let [{:keys [entities
                pressed-keys
                player-x
                player-y
                direction
                player-images
                player-image-key]
         :as state} @*state
        game-width (utils/get-width game)
        game-height (utils/get-height game)]
    ;; render the blue background
    (c/render game (update screen-entity :viewport
                           assoc :width game-width :height game-height))
    ;; get the current player image to display
    (when-let [player (get player-images player-image-key)]
      (let [player-width (/ game-width 10)
            player-height (* player-width (/ (:height player) (:width player)))]
        ;; render the player
        (doseq [entity (transform
                         [{:project {:width game-width
                                     :height game-height}
                           :translate {:x (cond-> player-x
                                                  (= direction :left)
                                                  (+ player-width))
                                       :y player-y}
                           :scale {:x (cond-> player-width
                                              (= direction :left)
                                              (* -1))
                                   :y player-height}}
                          player])]
          (c/render game entity))
        ;; change the state to move the player
        (swap! *state
          (fn [state]
            (->> (assoc state
                        :player-width player-width
                        :player-height player-height)
                 (move/move game)
                 (move/prevent-move game)
                 (move/animate game)))))))
  ;; return the game map
  game)

