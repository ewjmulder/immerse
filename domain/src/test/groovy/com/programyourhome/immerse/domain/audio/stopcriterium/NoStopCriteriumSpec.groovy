package com.programyourhome.immerse.domain.audio.stopcriterium

import spock.lang.Specification

class NoStopCriteriumSpec extends Specification {

    def "No stop criterium should always return false"() {
        expect:
        !new NoStopCriterium().shouldStop()
    }
}
