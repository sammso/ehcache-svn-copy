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

package net.sf.ehcache.store.compound.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfigurationListener;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.SearchException;
import net.sf.ehcache.search.aggregator.AggregatorInstance;
import net.sf.ehcache.search.attribute.AttributeExtractor;
import net.sf.ehcache.search.attribute.AttributeType;
import net.sf.ehcache.search.expression.Criteria;
import net.sf.ehcache.search.impl.OrderComparator;
import net.sf.ehcache.search.impl.ResultImpl;
import net.sf.ehcache.search.impl.ResultsImpl;
import net.sf.ehcache.store.ElementAttributeValues;
import net.sf.ehcache.store.FifoPolicy;
import net.sf.ehcache.store.LfuPolicy;
import net.sf.ehcache.store.LruPolicy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.sf.ehcache.store.Policy;
import net.sf.ehcache.store.StoreQuery;
import net.sf.ehcache.store.StoreQuery.Ordering;
import net.sf.ehcache.store.compound.CompoundStore;
import net.sf.ehcache.store.compound.factories.CapacityLimitedInMemoryFactory;

/**
 * Implements a memory only store.
 *
 * @author Chris Dennis
 */
public final class MemoryOnlyStore extends CompoundStore implements CacheConfigurationListener {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    private final Map<String, AttributeExtractor> attributeExtractors = new ConcurrentHashMap<String, AttributeExtractor>();

    private final CapacityLimitedInMemoryFactory memoryFactory;

    private final CacheConfiguration config;

    private final Map<String, Attribute> searchAttributes = new ConcurrentHashMap<String, Attribute>();

    private MemoryOnlyStore(CapacityLimitedInMemoryFactory memory, CacheConfiguration config) {
        super(memory, config.isCopyOnRead(), config.isCopyOnWrite(), config.getCopyStrategy());
        this.memoryFactory = memory;
        this.config = config;
    }

    /**
     * Constructs an in-memory store for the given cache, using the given disk path.
     *
     * @param cache cache that fronts this store
     * @param diskStorePath disk path to store data in
     * @return a fully initialized store
     */
    public static MemoryOnlyStore create(Cache cache, String diskStorePath) {
        CacheConfiguration config = cache.getCacheConfiguration();
        CapacityLimitedInMemoryFactory memory = new CapacityLimitedInMemoryFactory(null, config.getMaxElementsInMemory(),
                determineEvictionPolicy(config), cache.getCacheEventNotificationService());
        MemoryOnlyStore store = new MemoryOnlyStore(memory, config);
        cache.getCacheConfiguration().addConfigurationListener(store);
        return store;
    }

    /**
     * Chooses the Policy from the cache configuration
     */
    private static final Policy determineEvictionPolicy(CacheConfiguration config) {
        MemoryStoreEvictionPolicy policySelection = config.getMemoryStoreEvictionPolicy();

        if (policySelection.equals(MemoryStoreEvictionPolicy.LRU)) {
            return new LruPolicy();
        } else if (policySelection.equals(MemoryStoreEvictionPolicy.FIFO)) {
            return new FifoPolicy();
        } else if (policySelection.equals(MemoryStoreEvictionPolicy.LFU)) {
            return new LfuPolicy();
        }

        throw new IllegalArgumentException(policySelection + " isn't a valid eviction policy");
    }

    /**
     * {@inheritDoc}
     */
    public boolean bufferFull() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKeyInMemory(Object key) {
        return containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKeyOffHeap(Object key) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKeyOnDisk(Object key) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void expireElements() {
        memoryFactory.expireElements();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This store is not persistent, so this simply clears the in-memory store if clear-on-flush is set for this cache.
     */
    public void flush() throws IOException {
        if (config.isClearOnFlush()) {
            removeAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Policy getInMemoryEvictionPolicy() {
        return memoryFactory.getEvictionPolicy();
    }

    /**
     * {@inheritDoc}
     */
    public int getInMemorySize() {
        return getSize();
    }

    /**
     * {@inheritDoc}
     */
    public long getInMemorySizeInBytes() {
        return memoryFactory.getSizeInBytes();
    }

    /**
     * {@inheritDoc}
     */
    public int getOnDiskSize() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getOnDiskSizeInBytes() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getOffHeapSize() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getOffHeapSizeInBytes() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getTerracottaClusteredSize() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setInMemoryEvictionPolicy(Policy policy) {
        memoryFactory.setEvictionPolicy(policy);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * A NO-OP
     */
    public void deregistered(CacheConfiguration config) {
        // no-op
    }

    /**
     * {@inheritDoc}
     * <p/>
     * A NO-OP
     */
    public void diskCapacityChanged(int oldCapacity, int newCapacity) {
        // no-op
    }

    /**
     * {@inheritDoc}
     * <p/>
     * A NO-OP
     */
    public void loggingChanged(boolean oldValue, boolean newValue) {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public void memoryCapacityChanged(int oldCapacity, int newCapacity) {
        memoryFactory.setCapacity(newCapacity);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * A NO-OP
     */
    public void registered(CacheConfiguration config) {
        // no-op
    }

    /**
     * {@inheritDoc}
     * <p/>
     * A NO-OP
     */
    public void timeToIdleChanged(long oldTimeToIdle, long newTimeToIdle) {
        // no-op
    }

    /**
     * {@inheritDoc}
     * <p/>
     * A NO-OP
     */
    public void timeToLiveChanged(long oldTimeToLive, long newTimeToLive) {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public Object getMBean() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributeExtractors(Map<String, AttributeExtractor> extractors) {
        this.attributeExtractors.putAll(extractors);

        for (String name : extractors.keySet()) {
            searchAttributes.put(name, new Attribute(name));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Results executeQuery(StoreQuery query) {
        Criteria c = query.getCriteria();

        List<AggregatorInstance<?>> aggregators = query.getAggregatorInstances();


        boolean includeResults = query.requestsKeys() || query.requestsValues() || !query.requestedAttributes().isEmpty();

        ArrayList<Result> results = new ArrayList<Result>();

        boolean hasOrder = !query.getOrdering().isEmpty();

        boolean anyMatches = false;

        for (Element element : elementSet()) {
            if (!hasOrder && query.maxResults() >= 0 && results.size() == query.maxResults()) {
                break;
            }

            ElementAttributeValues elementAttributeValues = new ElementAttributeValuesImpl(element, attributeExtractors);

            boolean match = c.execute(element, elementAttributeValues);

            if (match) {
                anyMatches = true;

                if (includeResults) {
                    final Map<String, Object> attributes;
                    if (query.requestedAttributes().isEmpty()) {
                        attributes = Collections.EMPTY_MAP;
                    } else {
                        attributes = new HashMap<String, Object>();
                        for (Attribute attribute : query.requestedAttributes()) {
                            String name = attribute.getAttributeName();
                            attributes.put(name, elementAttributeValues.getAttributeValue(name));
                        }
                    }

                    final Object[] sortAttributes;
                    List<Ordering> orderings = query.getOrdering();
                    if (orderings.isEmpty()) {
                        sortAttributes = EMPTY_OBJECT_ARRAY;
                    } else {
                        sortAttributes = new Object[orderings.size()];
                        for (int i = 0; i < sortAttributes.length; i++) {
                            String name = orderings.get(i).getAttribute().getAttributeName();
                            sortAttributes[i] = elementAttributeValues.getAttributeValue(name);
                        }
                    }


                    results.add(new ResultImpl(element.getObjectKey(), element.getObjectValue(), query, attributes, sortAttributes));
                }

                for (AggregatorInstance<?> aggregator : aggregators) {
                    Attribute<?> attribute = aggregator.getAttribute();
                    if (attribute == null) {
                        aggregator.accept(null);
                    } else {
                        Object val = elementAttributeValues.getAttributeValue(attribute.getAttributeName());
                        aggregator.accept(val);
                    }
                }
            }
        }

        if (hasOrder) {
            Collections.sort(results, new OrderComparator(query.getOrdering()));

            // trim results to max length if necessary
            int max = query.maxResults();
            if (max >= 0 && (results.size() > max)) {
                int trim = results.size() - max;
                for (int i = 0; i < trim; i++) {
                    results.remove(results.size() - 1);
                }
                results.trimToSize();
            }
        }


        List<Object> aggregateResults = aggregators.isEmpty() ? Collections.EMPTY_LIST : new ArrayList<Object>();
        for (AggregatorInstance<?> aggregator : aggregators) {
            aggregateResults.add(aggregator.aggregateResult());
        }

        if (anyMatches && !includeResults && !aggregateResults.isEmpty()) {
            // add one row in the results if the only thing included was aggregators and anything matched
            results.add(new ResultImpl(null, null, query, Collections.EMPTY_MAP, EMPTY_OBJECT_ARRAY));
        }


        if (!aggregateResults.isEmpty()) {
            for (Result result : results) {
                // XXX: yucky cast
                ((ResultImpl)result).setAggregateResults(aggregateResults);
            }
        }

        return new ResultsImpl(results, query.requestsKeys(), !query.requestedAttributes().isEmpty(), anyMatches && !aggregateResults.isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Attribute<T> getSearchAttribute(String attributeName) throws CacheException {
        return searchAttributes.get(attributeName);
    }

    /**
     * Implementation for {@link ElementAttributeValues}. Caches repeated reads and type lookups
     */
    private static class ElementAttributeValuesImpl implements ElementAttributeValues {

        private static final Object NULL = new Object();

        private final Map<String, TypedValue> cache = new HashMap<String, TypedValue>();
        private final Element element;
        private final Map<String, AttributeExtractor> attributeExtractors;

        public ElementAttributeValuesImpl(Element element, Map<String, AttributeExtractor> attributeExtractors) {
            this.element = element;
            this.attributeExtractors = attributeExtractors;
        }

        /**
         * {@inheritDoc}
         */
        public Object getAttributeValue(String attributeName) throws SearchException {
            return getAttributeValue(attributeName, null, false);
        }

        /**
         * {@inheritDoc}
         */
        public Object getAttributeValue(String attributeName, AttributeType expectedType) throws SearchException {
            return getAttributeValue(attributeName, expectedType, true);
        }

        private Object getAttributeValue(String attributeName, AttributeType expectedType, boolean checkType) throws SearchException {
            TypedValue cachedValue = cache.get(attributeName);
            if (cachedValue != null) {
                if (checkType) {
                    return cachedValue.getValue(expectedType);
                } else {
                    return cachedValue.getValue();
                }
            }

            AttributeExtractor extractor = attributeExtractors.get(attributeName);
            if (extractor == null) {
                throw new SearchException("No such search attribute named [" + attributeName + "]");
            }

            Object value = extractor.attributeFor(element);
            if (value instanceof String) {
                value = ((String) value).toLowerCase();
            }

            if (value == null) {
                cache.put(attributeName, new TypedValue(attributeName, NULL, null));
            } else {
                AttributeType actualType = AttributeType.typeFor(attributeName, value);

                if (checkType) {
                    if (actualType != expectedType) {
                        throw new SearchException("Expecting attribute of type " + expectedType.name() + " but was " + actualType.name());
                    }
                }

                cache.put(attributeName, new TypedValue(attributeName, value, actualType));
            }
            return value;
        }

        /**
         * A cached attribute value and type lookup
         */
        private static class TypedValue {
            private final AttributeType type;
            private final Object value;
            private final String attributeName;

            TypedValue(String attributeName, Object value, AttributeType type) {
                this.attributeName = attributeName;
                this.value = value;
                this.type = type;
            }

            public Object getValue() {
                if (value == NULL) {
                    return null;
                }

                return value;
            }

            public Object getValue(AttributeType expectedType) {
                if (value == NULL) {
                    return null;
                }

                if (type != expectedType) {
                    throw new SearchException("Expecting value of type (" + expectedType + ") for attribute [" + attributeName
                            + "] but was (" + type + ")");
                }

                return value;
            }
        }

    }

}
