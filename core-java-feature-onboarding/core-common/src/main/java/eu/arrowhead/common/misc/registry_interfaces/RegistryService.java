package eu.arrowhead.common.misc.registry_interfaces;

import eu.arrowhead.common.exception.ArrowheadException;
import javax.validation.Valid;

public interface RegistryService<T> {

  T lookup(final long id) throws ArrowheadException;

  T publish(@Valid final T entity) throws ArrowheadException;

  T unpublish(@Valid final T entity) throws ArrowheadException;
}
