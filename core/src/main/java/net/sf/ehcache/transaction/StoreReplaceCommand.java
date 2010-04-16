/**
 *  Copyright 2003-2010 Terracotta, Inc.
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
package net.sf.ehcache.transaction;

import net.sf.ehcache.Element;
import net.sf.ehcache.store.Store;
import net.sf.ehcache.writer.CacheWriterManager;

/**
 * @author Alex Snaps
 */
public class StoreReplaceCommand implements StoreWriteCommand {

    private final Element element;

    /**
     * Constructor
     * @param element the element to replace an existing entry
     */
    public StoreReplaceCommand(final Element element) {
        this.element = element;
    }

    /**
     * {@inheritDoc}
     */
    public String getCommandName() {
        return REPLACE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute(final Store store) {
        return store.replace(element) != null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute(final CacheWriterManager cacheWriterManager) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPut(final Object key) {
        return element.getKey().equals(key);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRemove(final Object key) {
        return false;
    }
}
