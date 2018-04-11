package com.programyourhome.immerse.domain.resource

import javax.sound.sampled.UnsupportedAudioFileException

import com.programyourhome.immerse.domain.audio.resource.FileAudioResource

import spock.lang.Specification

class FileAudioResourceSpec extends Specification {

    def "Correct audio file should result in correct audio stream"() {
        given:
        def audioResource = new FileAudioResource("src/test/resources/clip-10ms.wav")

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

        when:
        new FileAudioResource("src/test/resources/bogus.wav")

        then:
        def ex = thrown(IllegalStateException)
        UnsupportedAudioFileException.class == ex.getCause().getClass()
    }

    def "Non existing file should result in an exception"() {
        given:

        when:
        new FileAudioResource("src/test/resources/does-not-exist.wav")

        then:
        thrown IllegalArgumentException
    }
}
