package com.canoo.dolphin.core

/**
 * A BasePresentationModel (PM) is an non-empty, unmodifiable collection of {@link BaseAttribute}s.
 * These Attributes may be backed by different beans and even beans of different types.
 * For convenience, PMs provide a method to change the backing beans of their Attributes.
 * This allows to bind against PMs (i.e. their Attributes) without the need for GRASP-like
 * PresentationModelSwitches.
 * PMs are not meant to be extended for the normal use.
 */

abstract class BasePresentationModel {
    protected List<BaseAttribute> attributes = new LinkedList<BaseAttribute>()
    final String id

    /** @throws AssertionError if the list of attributes is null or empty  **/
    BasePresentationModel(List<BaseAttribute> attributes) {
        this(null, attributes)
    }
    
    /** @throws AssertionError if the list of attributes is null or empty  **/
    BasePresentationModel(String id, List<BaseAttribute> attributes) {
        assert attributes
        this.id = id ?: makeId(this)
        this.attributes.addAll(attributes)
        this.attributes = this.attributes.asImmutable()
    }

    /** @return the immutable internal representation */
    List<BaseAttribute> getAttributes() {
        attributes
    }
    
    protected static String makeId(BasePresentationModel instance) {
        System.identityHashCode(instance).toString()
    } 
}