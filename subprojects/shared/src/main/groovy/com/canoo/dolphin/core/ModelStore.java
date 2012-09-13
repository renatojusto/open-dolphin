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

package com.canoo.dolphin.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ModelStore {

    // todo dk: this doesn't need to be ConcurrentHashMaps since we are thread-confined
    private final Map<String, PresentationModel> presentationModels = new ConcurrentHashMap<String, PresentationModel>();
    private final Map<String, List<PresentationModel>> modelsPerType = new ConcurrentHashMap<String, List<PresentationModel>>();
    private final Map<Long, Attribute> attributesPerId = new ConcurrentHashMap<Long, Attribute>();
    private final Map<String, List<Attribute>> attributesPerQualifier = new ConcurrentHashMap<String, List<Attribute>>();

    private final PropertyChangeListener ATTRIBUTE_WORKER = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            Attribute attribute = (Attribute) event.getSource();
            String oldQualifier = (String) event.getOldValue();
            String newQualifier = (String) event.getNewValue();

            if (null != oldQualifier) removeAttributeByQualifier(attribute, oldQualifier);
            if (null != newQualifier) addAttributeByQualifier(attribute);
        }
    };

    public Set<String> listPresentationModelIds() {
        return presentationModels.keySet();
    }
    public Collection<PresentationModel> listPresentationModels() {
        return presentationModels.values();
    }

    public boolean add(PresentationModel model) {
        if (null == model) return false;

        if (presentationModels.containsKey(model.getId())) {
            throw new IllegalArgumentException("there already is a PM with id " + model.getId());
        }
        boolean added = false;
        if (!presentationModels.containsValue(model)) {
            presentationModels.put(model.getId(), model);
            addPresentationModelByType(model);
            for (Attribute attribute : model.getAttributes()) {
                addAttributeById(attribute);
                attribute.addPropertyChangeListener(Attribute.QUALIFIER_PROPERTY, ATTRIBUTE_WORKER);
                if (!isBlank(attribute.getQualifier())) addAttributeByQualifier(attribute);
            }
            added = true;
        }
        return added;
    }

    public boolean remove(PresentationModel model) {
        if (null == model) return false;
        boolean removed = false;
        if (presentationModels.containsValue(model)) {
            removePresentationModelByType(model);
            presentationModels.remove(model.getId());
            for (Attribute attribute : model.getAttributes()) {
                removeAttributeById(attribute);
                attribute.removePropertyChangeListener(Attribute.QUALIFIER_PROPERTY, ATTRIBUTE_WORKER);
                if (!isBlank(attribute.getQualifier())) removeAttributeByQualifier(attribute);
            }
            removed = true;
        }
        return removed;
    }

    protected void addAttributeById(Attribute attribute) {
        if (null == attribute || attributesPerId.containsKey(attribute.getId())) return;
        attributesPerId.put(attribute.getId(), attribute);
    }

    protected void removeAttributeById(Attribute attribute) {
        if (null == attribute) return;
        attributesPerId.remove(attribute.getId());
    }

    protected void addAttributeByQualifier(Attribute attribute) {
        if (null == attribute) return;
        String qualifier = attribute.getQualifier();
        if (isBlank(qualifier)) return;
        List<Attribute> list = attributesPerQualifier.get(qualifier);
        if (null == list) {
            list = new ArrayList<Attribute>();
            attributesPerQualifier.put(qualifier, list);
        }
        if (!list.contains(attribute)) list.add(attribute);
    }

    protected void removeAttributeByQualifier(Attribute attribute) {
        if (null == attribute) return;
        String qualifier = attribute.getQualifier();
        if (isBlank(qualifier)) return;
        List<Attribute> list = attributesPerQualifier.get(qualifier);
        if (null != list) {
            list.remove(attribute);
        }
    }

    protected void addPresentationModelByType(PresentationModel model) {
        if (null == model) return;
        String type = model.getPresentationModelType();
        if (isBlank(type)) return;
        List<PresentationModel> list = modelsPerType.get(type);
        if (null == list) {
            list = new ArrayList<PresentationModel>();
            modelsPerType.put(type, list);
        }
        if (!list.contains(model)) list.add(model);
    }

    protected void removePresentationModelByType(PresentationModel model) {
        if (null == model) return;
        String type = model.getPresentationModelType();
        if (isBlank(type)) return;
        List<PresentationModel> list = modelsPerType.get(type);
        if (null != list) {
            list.remove(model);
        }
    }

    protected void removeAttributeByQualifier(Attribute attribute, String qualifier) {
        if (isBlank(qualifier)) return;
        List<Attribute> list = attributesPerQualifier.get(qualifier);
        if (null == list) return;
        list.remove(attribute);
    }

    public PresentationModel findPresentationModelById(String id) {
        return presentationModels.get(id);
    }

    public List<PresentationModel> findAllPresentationModelsByType(String type) {
        if (isBlank(type) || !modelsPerType.containsKey(type)) return Collections.emptyList();
        return Collections.unmodifiableList(modelsPerType.get(type));
    }

    public boolean containsPresentationModel(String id) {
        return presentationModels.containsKey(id);
    }

    public Attribute findAttributeById(long id) {
        return attributesPerId.get(id);
    }

    public List<Attribute> findAllAttributesByQualifier(String qualifier) {
        if (isBlank(qualifier) || !attributesPerQualifier.containsKey(qualifier)) return Collections.emptyList();
        return Collections.unmodifiableList(attributesPerQualifier.get(qualifier));
    }

    public void registerAttribute(Attribute attribute) {
        if (null == attribute) return;
        boolean listeningAlready = false;
        for (PropertyChangeListener listener : attribute.getPropertyChangeListeners(Attribute.QUALIFIER_PROPERTY)) {
            if (ATTRIBUTE_WORKER == listener) {
                listeningAlready = true;
                break;
            }
        }

        if (!listeningAlready) {
            attribute.addPropertyChangeListener(Attribute.QUALIFIER_PROPERTY, ATTRIBUTE_WORKER);
        }

        addAttributeByQualifier(attribute);
        addAttributeById(attribute);
    }

    public static boolean isBlank(String str) {
        return null == str || str.trim().length() == 0;
    }
}
