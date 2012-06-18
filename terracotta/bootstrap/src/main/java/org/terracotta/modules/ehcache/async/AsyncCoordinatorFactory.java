/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.modules.ehcache.async;

import net.sf.ehcache.Ehcache;

public interface AsyncCoordinatorFactory {
  AsyncCoordinator getOrCreateAsyncCoordinator(final String asyncName, final Ehcache cache, final AsyncConfig config);
}