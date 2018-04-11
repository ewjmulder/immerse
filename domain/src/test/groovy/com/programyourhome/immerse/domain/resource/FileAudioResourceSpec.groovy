package com.programyourhome.immerse.domain.resource

import javax.sound.sampled.UnsupportedAudioFileException

import com.programyourhome.immerse.domain.audio.resource.FileAudioResource

import spock.lang.Specification

class FileAudioResourceSpec extends Specification {

    def "Correct audio file should result in correct audio stream"() {
        given:
        def audioResource = new FileAudioResource("src/test/resources/clip-10ms.wav")

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
        def audioResource = new FileAudioResource("src/test/resources/bogus.wav")

        when:
        def audioStream = audioResource.getAudioInputStreamSupplier().get()

        then:
        def ex = thrown(IllegalStateException)
        ex.getCause().getClass() == UnsupportedAudioFileException.class
    }

    def "Non existing file should result in an exception"() {
        given:

        when:
        def audioResource = new FileAudioResource("src/test/resources/does-not-exist.wav")

        then:
        thrown IllegalArgumentException
    }
}
