package com.programyourhome.immerse.toolbox.audio.playback

import spock.lang.Specification

class ForeverPlaybackSpec extends Specification {

    def "Forever playback should not stop before audio start"() {
        when:
        def playback = new ForeverPlayback()

        then:
        !playback.shouldStop()
    }

    def "Forever playback should not stop after audio start and end of stream"() {
        given:
        def playback = new ForeverPlayback()

        when:
        playback.audioStarted()

        then:
        !playback.shouldStop()
        playback.endOfStream()
        !playback.shouldStop()
    }
}
