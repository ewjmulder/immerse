package com.programyourhome.immerse.toolbox.audio.playback

import spock.lang.Specification

// Slightly tricky to test timer logic in unit tests, but should be stable with the right boundary settings
class TimerPlaybackSpec extends Specification {

    def "Times playback of 100 millis should not stop directly after audio start"() {
        given:
        def playback = new TimerPlayback(100)

        when:
        playback.audioStarted()

        then:
        !playback.shouldStop()
    }

    def "Times playback of 100 millis should stop after 105 millis"() {
        given:
        def playback = new TimerPlayback(100)

        when:
        playback.audioStarted()
        Thread.sleep(105)

        then:
        playback.shouldStop()
    }

    def "Timer playback of -1 should throw an exception"() {
        when:
        def playback = new TimerPlayback(-1)

        then:
        thrown(IllegalArgumentException)
    }
}
