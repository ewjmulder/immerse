package com.programyourhome.immerse.domain.speakers.algorithms

import com.programyourhome.immerse.domain.ImmerseSettings
import com.programyourhome.immerse.domain.Room
import com.programyourhome.immerse.domain.Scene
import com.programyourhome.immerse.domain.audio.stopcriterium.NoStopCriterium
import com.programyourhome.immerse.domain.location.Vector3D
import com.programyourhome.immerse.domain.speakers.Speaker
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.OnlyClosestSpeakerVolumeRatiosAlgorithm

import spock.lang.Specification

class FieldOfHearingFractionalRatiosAlgorithmSpec extends Specification {

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
		algorithm.calculateVolumeRatios(scene).getVolume(1) == v1
		algorithm.calculateVolumeRatios(scene).getVolume(2) == v2
		
        where:
        x1 | y1 | z1 | x2 | y2 | z2 || v1  | v2
        0  | 10 | 10 | 10 | 10 | 0  || 0.5 | 0.5    // same distance
	}
	
}