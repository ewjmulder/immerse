package com.programyourhome.immerse.domain.resource

import javax.sound.sampled.UnsupportedAudioFileException

import com.programyourhome.immerse.domain.audio.resource.UrlAudioResource

import spock.lang.Specification

class UrlAudioResourceSpec extends Specification {

    def "Correct audio URL should result in correct audio stream"() {
        given:
        def url = getClass().getClassLoader().getResource("clip-10ms.wav")
        def audioResource = new UrlAudioResource(url)

        when:
        def audioStream = audioResource.getAudioInputStreamSupplier().get()

        then:
        audioStream.available() > 0
        audioStream.read(new byte[2])
        audioStream.close()
        noExceptionThrown()
    }

    def "Incorrect audio file should result in an exception"() {
        given:
        def url = getClass().getClassLoader().getResource("bogus.wav")
        def audioResource = new UrlAudioResource(url)

        when:
        def audioStream = audioResource.getAudioInputStreamSupplier().get()

        then:
        def ex = thrown(IllegalStateException)
        ex.getCause().getClass() == UnsupportedAudioFileException.class
    }

    def "Incorrect audio url should result in an exception"() {
        given:

        when:
        def audioResource = new UrlAudioResource("unknown://protocol")

        then:
        thrown IllegalArgumentException
    }
}
