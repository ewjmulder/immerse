package com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios

import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.OnlyClosestVolumeRatiosAlgorithm.onlyClosest
import static com.programyourhome.immerse.toolbox.util.TestData.listener
import static com.programyourhome.immerse.toolbox.util.TestData.room
import static com.programyourhome.immerse.toolbox.util.TestData.settings
import static com.programyourhome.immerse.toolbox.util.TestData.snapshot
import static com.programyourhome.immerse.toolbox.util.TestData.source
import static com.programyourhome.immerse.toolbox.util.TestData.speaker

import spock.lang.Specification

class OnlyClosestVolumeRatiosAlgorithmSpec extends Specification {

    //TODO: this is not the right test, since the algo uses the underlying calculateAngleInDegrees, that should be tested separately!
    def "Closest angle speaker should be selected"() {
        given:
        def room = room(speaker(1, x1, y1, z1), speaker(2, x2, y2, z2))
        def snapshot = snapshot(source(10, 10, 10), listener(0, 0, 0), settings())

        expect:
        onlyClosest().create().calculateVolumeRatios(room, snapshot).getVolumeRatio(1) == v1
        onlyClosest().create().calculateVolumeRatios(room, snapshot).getVolumeRatio(2) == v2

        where:
        x1 | y1 | z1 | x2 | y2 | z2 || v1 | v2
        10 | 10 | 9  | 10 | 10 | 8  || 1  | 0    // speaker 1 closer
        10 | 10 | 10 | 10 | 10 | 9  || 1  | 0    // speaker 1 exactly at source position
        1  | 1  | 1  | 10 | 10 | 9  || 1  | 0    // speaker 1 exactly in source direction, but at another position
        0  | 0  | 0  | 10 | 10 | 9  || 0  | 1    // speaker 1 exactly at listener position (will 'disable' speaker 1)
        10 | 10 | 9  | 10 | 9  | 10 || 1  | 0    // speaker 1 and 2 at exactly the same distance, always take first
        -10|-10 | -10|-10 | -10| -9 || 0  | 1    // speaker 1 exactly opposite to source
    }

}