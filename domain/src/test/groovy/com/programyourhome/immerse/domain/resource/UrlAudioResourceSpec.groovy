package com.programyourhome.immerse.domain.resource

import com.programyourhome.immerse.domain.audio.resource.UrlAudioResource

import spock.lang.Specification

class UrlAudioResourceSpec extends Specification {

    def "Correct audio URL should result in correct audio stream"() {
        given:
        def url = getClass().getClassLoader().getResource("clip-10ms.wav");
        def audioResource = new UrlAudioResource(url);

        when:
        def audioStream = audioResource.constructAudioStream();

        then:
        audioStream.available() > 0
        audioStream.read(new byte[2])
        audioStream.close()
        noExceptionThrown()
    }

    def "Incorrect audio url should result in an exception"() {
        given:
        def url = getClass().getClassLoader().getResource("bogus.wav");
        def audioResource = new UrlAudioResource(url);

        when:
        def audioStream = audioResource.constructAudioStream();

        then:
        thrown IOException
    }
}
