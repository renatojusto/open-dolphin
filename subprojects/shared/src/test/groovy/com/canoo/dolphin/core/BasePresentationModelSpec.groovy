/*
 * Copyright 2012 Canoo Engineering AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.canoo.dolphin.core

import spock.lang.Specification
import java.beans.PropertyChangeListener

class BasePresentationModelSpec extends Specification {
    def "attributes are accessible as properties"() {
        given:

        def baseAttribute = new MyAttribute('myPropName')
        def pm = new BasePresentationModel([baseAttribute])

        expect:

        pm.attributes.find { it.propertyName == 'myPropName' } == baseAttribute // old style
        pm.myPropName == baseAttribute  // new style
    }

    def "missing attributes throw MissingPropertyException on access"() {
        given:
        def baseAttribute = new MyAttribute('myPropName')
        def pm = new BasePresentationModel([baseAttribute])

        when:
        pm.noSuchAttributeName

        then:
        def exception = thrown(MissingPropertyException)
        exception.message.contains('noSuchAttributeName')
    }

    def "dirty attributes make the pm dirty too"() {
        given:

        def attr1 = new MyAttribute('one')
        def attr2 = new MyAttribute('two', 2)
        def model = new BasePresentationModel('model', [attr1, attr2])
        def changeListener = Mock(PropertyChangeListener)
        model.addPropertyChangeListener(PresentationModel.DIRTY_PROPERTY, changeListener)

        assert !attr1.dirty
        assert !attr2.dirty
        assert !model.dirty

        when:

        attr1.value = 1

        then:

        1 * changeListener.propertyChange(_)
        attr1.dirty
        model.dirty

        when:

        attr1.value = null

        then:

        1 * changeListener.propertyChange(_)
        !attr1.dirty
        !model.dirty

        when:

        attr2.value = 2

        then:

        0 * changeListener.propertyChange(_)
        !attr2.dirty
        !model.dirty

        when:

        attr2.value = 3

        then:

        1 * changeListener.propertyChange(_)
        attr2.dirty
        model.dirty

        when:

        attr1.value = 4

        then:

        0 * changeListener.propertyChange(_)
        attr1.dirty
        attr2.dirty
        model.dirty
    }
}