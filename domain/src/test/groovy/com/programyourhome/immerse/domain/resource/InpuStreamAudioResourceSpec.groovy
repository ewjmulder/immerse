package com.programyourhome.immerse.domain.resource

import com.programyourhome.immerse.domain.audio.resource.InputStreamAudioResource

import spock.lang.Specification

class InpuStreamAudioResourceSpec extends Specification {

    def "Correct audio input stream should result in correct audio stream"() {
        given:
        def inpustream = getClass().getResourceAsStream("/clip-10ms.wav");
        def audioResource = new InputStreamAudioResource(inpustream);

        when:
        def audioStream = audioResource.getAudioStream();

        then:
        audioStream.available() > 0
        audioStream.read(new byte[2])
        audioStream.close()
        noExceptionThrown()
    }

    def "Incorrect audio url should result in an exception"() {
        given:
        def inpustream = getClass().getResourceAsStream("/bogus.wav");
        def audioResource = new InputStreamAudioResource(inpustream);

        when:
        def audioStream = audioResource.getAudioStream();

        then:
        thrown IOException
    }
}
