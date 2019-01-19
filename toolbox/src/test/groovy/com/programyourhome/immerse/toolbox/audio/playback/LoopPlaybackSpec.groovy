package com.programyourhome.immerse.toolbox.audio.playback

import spock.lang.Specification

class LoopPlaybackSpec extends Specification {

    def "Loop playback of 5 should not stop after audio start and should stop after 5 times end of stream"() {
        given:
        def playback = new LoopPlayback(5)

        when:
        playback.audioStarted()

        then:
        !playback.shouldStop()
        playback.endOfStream()
        playback.endOfStream()
        playback.endOfStream()
        playback.endOfStream()
        !playback.endOfStream()
    }

    def "Loop playback of 0 should throw an exception"() {
        when:
        def playback = new LoopPlayback(0)

        then:
        thrown(IllegalArgumentException)
    }
}
