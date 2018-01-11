package com.programyourhome.immerse.domain.speakers.algorithms

import com.programyourhome.immerse.domain.ImmerseSettings
import com.programyourhome.immerse.domain.Room
import com.programyourhome.immerse.domain.Scene
import com.programyourhome.immerse.domain.audio.stopcriterium.NoStopCriterium
import com.programyourhome.immerse.domain.location.Vector3D
import com.programyourhome.immerse.domain.speakers.Speaker
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.OnlyClosestSpeakerVolumeRatiosAlgorithm

import spock.lang.Specification

class OnlyClosestSpeakerRatiosAlgorithmSpec extends Specification {

	//TODO: this is not the right test, since the algo uses the underlying calculateAngleInDegrees, that should be tested separately!
	def "Closest angle speaker should be selected"() {
		given:
		def speaker1 = new Speaker(1, "1", "", 1, new Vector3D(x1, y1, z1))
		def speaker2 = new Speaker(2, "2", "", 1, new Vector3D(x2, y2, z2))
		def listener = new Vector3D(0, 0, 0)
		def source = new Vector3D(10, 10, 10)
		
		def algorithm = new OnlyClosestSpeakerVolumeRatiosAlgorithm()
		def room = new Room("Test", "")
		room.addSpeaker(speaker1)
		room.addSpeaker(speaker2)
		def settings = new ImmerseSettings(algorithm, new NoStopCriterium())
		def scene = new Scene(room, listener, source, settings)
		def speakerVolumes = algorithm.calculateVolumeRatios(scene)
		
		expect:
		algorithm.calculateVolumeRatios(scene).getVolumeRatio(1) == v1
		algorithm.calculateVolumeRatios(scene).getVolumeRatio(2) == v2
		
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