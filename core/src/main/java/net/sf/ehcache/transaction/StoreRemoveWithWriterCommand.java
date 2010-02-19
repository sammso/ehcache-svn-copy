/**
 *  Copyright 2003-2009 Terracotta, Inc.
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

import net.sf.ehcache.writer.CacheWriterManager;

/**
 * @author Alex Snaps
 */
public class StoreRemoveWithWriterCommand extends StoreRemoveCommand {

    /**
     * Constructs a remove command for a key
     *
     * @param key to remove from the store on {@link StorePutCommand#execute(net.sf.ehcache.store.Store)}
     */
    public StoreRemoveWithWriterCommand(final Object key) {
        super(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final CacheWriterManager cacheWriterManager) {
        cacheWriterManager.remove(getKey());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommandName() {
        return Command.REMOVE_WITH_WRITER;
    }
}
