(ns play-cljc.examples-advanced
  (:require [play-cljc.core :as c]
            [play-cljc.utils :as u]
            [play-cljc.example-utils :as eu]
            [play-cljc.example-data :as data]
            [play-cljc.primitives :as primitives]
            [play-cljc.math :as m]
            #?(:clj  [play-cljc.macros-java :refer [gl math]]
               :cljs [play-cljc.macros-js :refer-macros [gl math]])
            #?(:clj [dynadoc.example :refer [defexample]]))
  #?(:cljs (:require-macros [dynadoc.example :refer [defexample]])))

(defn advanced-render [game [entity objects {:keys [then now] :as state}]]
  (eu/resize-example game)
  (c/render-entity game
    {:clear {:color [1 1 1 1] :depth 1}})
  (let [projection-matrix (m/perspective-matrix-3d {:field-of-view (m/deg->rad 60)
                                                    :aspect (/ (eu/get-width game)
                                                               (eu/get-height game))
                                                    :near 1
                                                    :far 2000})
        camera-pos [0 0 100]
        target [0 0 0]
        up [0 1 0]
        camera-matrix (m/look-at camera-pos target up)
        view-matrix (m/inverse-matrix 4 camera-matrix)
        view-projection-matrix (m/multiply-matrices 4 view-matrix projection-matrix)
        entity (assoc entity
                 :uniforms {'u_lightWorldPos [-50 30 100]
                            'u_viewInverse camera-matrix
                            'u_lightColor [1 1 1 1]})]
    (doseq [{:keys [rx ry tz mat-uniforms]}
            objects]
      (let [world-matrix (->> (m/identity-matrix-3d)
                              (m/multiply-matrices 4 (m/x-rotation-matrix-3d (* rx now)))
                              (m/multiply-matrices 4 (m/y-rotation-matrix-3d (* ry now)))
                              (m/multiply-matrices 4 (m/translation-matrix-3d 0 0 tz)))]
        (c/render-entity game
          (-> entity
              (assoc :viewport {:x 0 :y 0 :width (eu/get-width game) :height (eu/get-height game)})
              (update :uniforms assoc
                'u_world world-matrix
                'u_worldViewProjection (->> view-projection-matrix
                                            (m/multiply-matrices 4 world-matrix))
                'u_worldInverseTranspose (->> world-matrix
                                              (m/inverse-matrix 4)
                                              (m/transpose-matrix-3d))
                'u_color (:u_color mat-uniforms)
                'u_specular (:u_specular mat-uniforms)
                'u_shininess (:u_shininess mat-uniforms)
                'u_specularFactor (:u_specularFactor mat-uniforms)))))))
  [entity objects (assoc state :then now :now (:time game))])

(defn shape-entity [game {:keys [positions normals texcoords indices]}]
  (c/create-entity game
    {:vertex data/advanced-vertex-shader
     :fragment data/advanced-fragment-shader
     :attributes {'a_position {:data positions
                               :type (gl game FLOAT)
                               :size 3}
                  'a_normal {:data normals
                             :type (gl game FLOAT)
                             :size 3}
                  'a_texCoord {:data texcoords
                               :type (gl game FLOAT)
                               :size 2}}
     :indices indices}))

;; balls-3d

(defn balls-3d-init [game]
  (gl game enable (gl game CULL_FACE))
  (gl game enable (gl game DEPTH_TEST))
  (let [entity (shape-entity game
                 (primitives/sphere {:radius 10 :subdivisions-axis 48 :subdivisions-height 24}))
        objects (vec
                  (for [i (range 100)]
                    {:tz (rand 150)
                     :rx (rand (* 2 (math PI)))
                     :ry (rand (math PI))
                     :mat-uniforms {:u_color           [(rand) (rand) (rand) 1]
                                    :u_specular        [1, 1, 1, 1]
                                    :u_shininess       (rand 500)
                                    :u_specularFactor  (rand 1)}}))
        state {:then 0
               :now 0}]
    [entity objects state]))

(defexample play-cljc.examples-advanced/balls-3d
  {:with-card card}
  (let [game (play-cljc.example-utils/init-example card)
        state (play-cljc.examples-advanced/balls-3d-init game)]
    (play-cljc.example-utils/game-loop
      play-cljc.examples-advanced/advanced-render
      game state)))

;; planes-3d

(defn planes-3d-init [game]
  (gl game enable (gl game CULL_FACE))
  (gl game enable (gl game DEPTH_TEST))
  (let [entity (shape-entity game
                 (primitives/plane {:width 20 :depth 20 :subdivisions-width 10 :subdivisions-height 10}))
        objects (vec
                  (for [i (range 100)]
                    {:tz (rand 150)
                     :rx (rand (* 2 (math PI)))
                     :ry (rand (math PI))
                     :mat-uniforms {:u_color           [(rand) (rand) (rand) 1]
                                    :u_specular        [1, 1, 1, 1]
                                    :u_shininess       (rand 500)
                                    :u_specularFactor  (rand 1)}}))
        state {:then 0
               :now 0}]
    [entity objects state]))

(defexample play-cljc.examples-advanced/planes-3d
  {:with-card card}
  (let [game (play-cljc.example-utils/init-example card)
        state (play-cljc.examples-advanced/planes-3d-init game)]
    (play-cljc.example-utils/game-loop
      play-cljc.examples-advanced/advanced-render
      game state)))

;; cubes-3d

(defn cubes-3d-init [game]
  (gl game enable (gl game CULL_FACE))
  (gl game enable (gl game DEPTH_TEST))
  (let [entity (shape-entity game
                 (primitives/cube {:size 20}))
        objects (vec
                  (for [i (range 100)]
                    {:tz (rand 150)
                     :rx (rand (* 2 (math PI)))
                     :ry (rand (math PI))
                     :mat-uniforms {:u_color           [(rand) (rand) (rand) 1]
                                    :u_specular        [1, 1, 1, 1]
                                    :u_shininess       (rand 500)
                                    :u_specularFactor  (rand 1)}}))
        state {:then 0
               :now 0}]
    [entity objects state]))

(defexample play-cljc.examples-advanced/cubes-3d
  {:with-card card}
  (let [game (play-cljc.example-utils/init-example card)
        state (play-cljc.examples-advanced/cubes-3d-init game)]
    (play-cljc.example-utils/game-loop
      play-cljc.examples-advanced/advanced-render
      game state)))

;; cylinder-3d

(defn cylinder-3d-init [game]
  (gl game enable (gl game CULL_FACE))
  (gl game enable (gl game DEPTH_TEST))
  (let [entity (shape-entity game
                 (primitives/cylinder {:bottom-radius 10 :top-radius 10 :height 30
                                       :radial-subdivisions 10 :vertical-subdivisions 10}))
        objects (vec
                  (for [i (range 100)]
                    {:tz (rand 150)
                     :rx (rand (* 2 (math PI)))
                     :ry (rand (math PI))
                     :mat-uniforms {:u_color           [(rand) (rand) (rand) 1]
                                    :u_specular        [1, 1, 1, 1]
                                    :u_shininess       (rand 500)
                                    :u_specularFactor  (rand 1)}}))
        state {:then 0
               :now 0}]
    [entity objects state]))

(defexample play-cljc.examples-advanced/cylinder-3d
  {:with-card card}
  (let [game (play-cljc.example-utils/init-example card)
        state (play-cljc.examples-advanced/cylinder-3d-init game)]
    (play-cljc.example-utils/game-loop
      play-cljc.examples-advanced/advanced-render
      game state)))

;; crescent-3d

(defn crescent-3d-init [game]
  (gl game enable (gl game CULL_FACE))
  (gl game enable (gl game DEPTH_TEST))
  (let [entity (shape-entity game
                 (primitives/crescent {:vertical-radius 20 :outer-radius 20 :inner-radius 15
                                       :thickness 10 :subdivisions-down 30}))
        objects (vec
                  (for [i (range 100)]
                    {:tz (rand 150)
                     :rx (rand (* 2 (math PI)))
                     :ry (rand (math PI))
                     :mat-uniforms {:u_color           [(rand) (rand) (rand) 1]
                                    :u_specular        [1, 1, 1, 1]
                                    :u_shininess       (rand 500)
                                    :u_specularFactor  (rand 1)}}))
        state {:then 0
               :now 0}]
    [entity objects state]))

(defexample play-cljc.examples-advanced/crescent-3d
  {:with-card card}
  (let [game (play-cljc.example-utils/init-example card)
        state (play-cljc.examples-advanced/crescent-3d-init game)]
    (play-cljc.example-utils/game-loop
      play-cljc.examples-advanced/advanced-render
      game state)))

;; torus-3d

(defn torus-3d-init [game]
  (gl game enable (gl game CULL_FACE))
  (gl game enable (gl game DEPTH_TEST))
  (let [entity (shape-entity game
                 (primitives/torus {:radius 20 :thickness 5
                                    :radial-subdivisions 20 :body-subdivisions 20}))
        objects (vec
                  (for [i (range 100)]
                    {:tz (rand 150)
                     :rx (rand (* 2 (math PI)))
                     :ry (rand (math PI))
                     :mat-uniforms {:u_color           [(rand) (rand) (rand) 1]
                                    :u_specular        [1, 1, 1, 1]
                                    :u_shininess       (rand 500)
                                    :u_specularFactor  (rand 1)}}))
        state {:then 0
               :now 0}]
    [entity objects state]))

(defexample play-cljc.examples-advanced/torus-3d
  {:with-card card}
  (let [game (play-cljc.example-utils/init-example card)
        state (play-cljc.examples-advanced/torus-3d-init game)]
    (play-cljc.example-utils/game-loop
      play-cljc.examples-advanced/advanced-render
      game state)))

;; disc-3d

(defn disc-3d-init [game]
  (gl game enable (gl game CULL_FACE))
  (gl game enable (gl game DEPTH_TEST))
  (let [entity (shape-entity game
                 (primitives/disc {:radius 20 :divisions 20}))
        objects (vec
                  (for [i (range 100)]
                    {:tz (rand 150)
                     :rx (rand (* 2 (math PI)))
                     :ry (rand (math PI))
                     :mat-uniforms {:u_color           [(rand) (rand) (rand) 1]
                                    :u_specular        [1, 1, 1, 1]
                                    :u_shininess       (rand 500)
                                    :u_specularFactor  (rand 1)}}))
        state {:then 0
               :now 0}]
    [entity objects state]))

(defexample play-cljc.examples-advanced/disc-3d
  {:with-card card}
  (let [game (play-cljc.example-utils/init-example card)
        state (play-cljc.examples-advanced/disc-3d-init game)]
    (play-cljc.example-utils/game-loop
      play-cljc.examples-advanced/advanced-render
      game state)))
