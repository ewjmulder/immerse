package com.programyourhome.immerse.domain.audio.stopcriterium

import spock.lang.Specification

class TimerStopCriteriumSpec extends Specification {

    def "Timer stop criterium should switch after time is up"() {
        given:
        def stopCriterium = new TimerStopCriterium(10);

        when:
        stopCriterium.audioStarted()

        then:
        //TODO: Sort of tricky time based test, could fail on a slow or busy computer, any other way to do this? By mocking System.currentTimeMillis?
        // Yes, there is: https://stackoverflow.com/questions/2001671/override-java-system-currenttimemillis-for-testing-time-sensitive-code
        !stopCriterium.shouldStop()
        Thread.sleep(11)
        stopCriterium.shouldStop()
    }
}
