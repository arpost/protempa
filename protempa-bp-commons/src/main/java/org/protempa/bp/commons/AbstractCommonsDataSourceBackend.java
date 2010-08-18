package org.protempa.bp.commons;

import org.protempa.AbstractDataSourceBackend;
import org.protempa.DataSourceBackendInitializationException;
import org.protempa.backend.BackendInstanceSpec;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractCommonsDataSourceBackend
        extends AbstractDataSourceBackend  {

    @Override
    public void initialize(BackendInstanceSpec config)
        throws DataSourceBackendInitializationException {
        CommonsBackend.initialize(this, config);
    }

    @Override
    public final String getDisplayName() {
        return CommonsBackend.backendInfo(this).displayName();
    }

    protected final String nameForErrors() {
        return CommonsBackend.nameForErrors(this);
    }
}
