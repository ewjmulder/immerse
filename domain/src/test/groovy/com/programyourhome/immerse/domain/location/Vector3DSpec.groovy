package com.programyourhome.immerse.domain.location
import spock.lang.Specification

class Vector3DSpec extends Specification {

    def "La4j Vector should contain correct values"() {
        given:
        def vector3D = new Vector3D(4, 5, 6)

        when:
        def vectorLa4j = vector3D.toLa4j()

        then:
        4 == vectorLa4j.get(0)
        5 == vectorLa4j.get(1)
        6 == vectorLa4j.get(2)
    }
}