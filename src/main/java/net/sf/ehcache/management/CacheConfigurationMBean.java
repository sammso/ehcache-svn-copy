/**
 *  Copyright 2003-2006 Greg Luck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.ehcache.management;

import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * A JMX MBean interface for the configuration of a cache
 * @author Greg Luck
 * @version $Id$
 */
public interface CacheConfigurationMBean {


    /**
     * Accessor
     */
    public String getName();

    /**
     * Accessor
     */
    public int getMaxElementsInMemory();

    /**
     * Accessor
     */
    public int getMaxElementsOnDisk();

    /**
     * Accessor
     */
    public MemoryStoreEvictionPolicy getMemoryStoreEvictionPolicy();

    /**
     * Accessor
     */
    public boolean isEternal();

    /**
     * Accessor
     */
    public long getTimeToIdleSeconds();

    /**
     * Accessor
     */
    public long getTimeToLiveSeconds();

    /**
     * Accessor
     */
    public boolean isOverflowToDisk();

    /**
     * Accessor
     */
    public boolean isDiskPersistent();

    /**
     * Accessor
     */
    public long getDiskExpiryThreadIntervalSeconds();


}
