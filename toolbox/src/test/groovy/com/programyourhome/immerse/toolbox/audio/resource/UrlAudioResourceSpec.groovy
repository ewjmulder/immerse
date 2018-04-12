package com.programyourhome.immerse.toolbox.audio.resource

import javax.sound.sampled.UnsupportedAudioFileException

import com.programyourhome.immerse.toolbox.audio.resource.UrlAudioResource

import spock.lang.Specification

class UrlAudioResourceSpec extends Specification {

    def "Correct audio URL should result in correct audio stream"() {
        given:
        def url = getClass().getClassLoader().getResource("clip-10ms.wav")
        def audioResource = new UrlAudioResource(url)

        when:
        def audioStream = audioResource.getAudioInputStream()

        then:
        audioStream.available() > 0
        audioStream.read(new byte[2])
        audioStream.close()
        noExceptionThrown()
    }

    def "Incorrect audio file should result in an exception"() {
        given:
        def url = getClass().getClassLoader().getResource("bogus.wav")

        when:
        def audioResource = new UrlAudioResource(url)

        then:
        def ex = thrown(IllegalStateException)
        UnsupportedAudioFileException.class == ex.getCause().getClass()
    }

    def "Incorrect audio url should result in an exception"() {
        given:

        when:
        new UrlAudioResource("unknown://protocol")

        then:
        thrown IllegalArgumentException
    }
}
