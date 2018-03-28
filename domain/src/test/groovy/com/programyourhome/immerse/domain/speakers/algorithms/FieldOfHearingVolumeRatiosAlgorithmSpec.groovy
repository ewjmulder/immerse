package com.programyourhome.immerse.domain.speakers.algorithms

import static com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm.onlyClosest
import static com.programyourhome.immerse.domain.util.TestData.listener
import static com.programyourhome.immerse.domain.util.TestData.room
import static com.programyourhome.immerse.domain.util.TestData.settings
import static com.programyourhome.immerse.domain.util.TestData.snapshot
import static com.programyourhome.immerse.domain.util.TestData.source
import static com.programyourhome.immerse.domain.util.TestData.speaker

import spock.lang.Specification

class FieldOfHearingVolumeRatiosAlgorithmSpec extends Specification {

    //TODO: test for this algo + abstract away all boilerplate
    def "Closest angle speaker should be selected"() {
        given:
        def snapshot = snapshot(
                room(speaker(1, x1, y1, z1), speaker(2, x2, y2, z2)),
                source(10, 10, 10), listener(0, 0, 0), settings())

        expect:
        onlyClosest().calculateVolumeRatios(snapshot).getVolumeRatio(1) == v1
        onlyClosest().calculateVolumeRatios(snapshot).getVolumeRatio(2) == v2

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