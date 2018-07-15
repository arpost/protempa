package org.protempa;

/*-
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 - 2018 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.event.DefaultWorkingMemoryEventListener;
import org.drools.event.ObjectRetractedEvent;
import org.protempa.proposition.Proposition;

/**
 * Listens for retracted propositions, and for propositions that were retracted
 * as a result of a proposition coming from a data source backend with its
 * delete date set, creates a copy of the proposition with its delete date set
 * to the date when the listener instance was created. Users of this class may
 * get the copies thus created using the {@link #getPropsToDelete() } method.
 *
 * @author Andrew Post
 */
final class DeletedWorkingMemoryEventListener extends DefaultWorkingMemoryEventListener {

    private final List<Proposition> propsToDelete;
    private final Logger logger;
    private final SetDeletedPropositionVisitor visitor;

    public DeletedWorkingMemoryEventListener() {
        this.propsToDelete = new ArrayList<>();
        this.logger = ProtempaUtil.logger();
        this.visitor = new SetDeletedPropositionVisitor();
    }

    /**
     * Listens for retracted propositions that were retracted as a result of
     * another proposition coming from a data source backend with a non-null
     * delete date, creates a copy of those propositions with their delete date
     * set to the date when the listener instance was created.
     *
     * @param ore
     */
    @Override
    public void objectRetracted(ObjectRetractedEvent ore) {
        String name = ore.getPropagationContext().getRuleOrigin().getName();
        if (name.equals("DELETE_PROPOSITION")) {
            Proposition prop = (Proposition) ore.getOldObject();
            this.logger.log(Level.FINEST, "Deleted proposition {0}", prop);
            if (prop.getDeleteDate() == null) {
                prop.accept(this.visitor);
                this.propsToDelete.add(this.visitor.getDeletedCopy());
            } else {
                this.propsToDelete.add(prop);
            }
        }
    }

    /**
     * Returns a copy of all retracted propositions that were retracted as a
     * result of another proposition coming from a data source backend with its
     * delete date set.
     *
     * @return a newly created list of propositions.
     */
    List<Proposition> getPropsToDelete() {
        return new ArrayList<>(this.propsToDelete);
    }

    /**
     * Clears the list of retracted propositions.
     */
    void clear() {
        this.propsToDelete.clear();
    }

}
