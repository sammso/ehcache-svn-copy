/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.modules.ehcache.store;

import net.sf.ehcache.config.NonstopConfiguration;
import net.sf.ehcache.config.TimeoutBehaviorConfiguration;

import org.terracotta.toolkit.nonstop.NonStopConfiguration;
import org.terracotta.toolkit.nonstop.NonStopConfigurationFields;

public class ToolkitNonStopConfiguration implements NonStopConfiguration {
  private final NonstopConfiguration ehcacheNonStopConfig;

  public ToolkitNonStopConfiguration(final NonstopConfiguration ehcacheNonStopConfig) {
    this.ehcacheNonStopConfig = ehcacheNonStopConfig;
  }

  @Override
  public NonStopConfigurationFields.NonStopReadTimeoutBehavior getImmutableOpNonStopTimeoutBehavior() {
    return convertEhcacheBehaviorToToolkitReadBehavior();
  }

  @Override
  public NonStopConfigurationFields.NonStopWriteTimeoutBehavior getMutableOpNonStopTimeoutBehavior() {
    return convertEhcacheBehaviorToToolkitWriteBehavior();
  }

  private NonStopConfigurationFields.NonStopReadTimeoutBehavior convertEhcacheBehaviorToToolkitReadBehavior() {
    TimeoutBehaviorConfiguration behaviorConfiguration = ehcacheNonStopConfig.getTimeoutBehavior();
    switch (behaviorConfiguration.getTimeoutBehaviorType()) {
      case EXCEPTION:
        return NonStopConfigurationFields.NonStopReadTimeoutBehavior.EXCEPTION_ON_TIMEOUT;
      case LOCAL_READS:
        return NonStopConfigurationFields.NonStopReadTimeoutBehavior.LOCAL_READS;
      case NOOP:
        return NonStopConfigurationFields.NonStopReadTimeoutBehavior.NO_OP;
      default:
        return NonStopConfigurationFields.NonStopReadTimeoutBehavior.EXCEPTION_ON_TIMEOUT;
    }
  }

  private NonStopConfigurationFields.NonStopWriteTimeoutBehavior convertEhcacheBehaviorToToolkitWriteBehavior() {
    TimeoutBehaviorConfiguration behaviorConfiguration = ehcacheNonStopConfig.getTimeoutBehavior();
    switch (behaviorConfiguration.getTimeoutBehaviorType()) {
      case EXCEPTION:
        return NonStopConfigurationFields.NonStopWriteTimeoutBehavior.EXCEPTION_ON_TIMEOUT;
      case LOCAL_READS:
        return NonStopConfigurationFields.NonStopWriteTimeoutBehavior.NO_OP;
      case NOOP:
        return NonStopConfigurationFields.NonStopWriteTimeoutBehavior.NO_OP;
      default:
        return NonStopConfigurationFields.NonStopWriteTimeoutBehavior.EXCEPTION_ON_TIMEOUT;
    }
  }

  @Override
  public long getTimeoutMillis() {
    return ehcacheNonStopConfig.getTimeoutMillis();
  }

  @Override
  public boolean isEnabled() {
    return ehcacheNonStopConfig.isEnabled();
  }

  @Override
  public boolean isImmediateTimeoutEnabled() {
    return ehcacheNonStopConfig.isImmediateTimeout();
  }

}