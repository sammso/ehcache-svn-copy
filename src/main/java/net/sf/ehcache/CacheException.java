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


package net.sf.ehcache;

/**
 * A Cache Exception.
 * @author Greg Luck
 * @version $Id: CacheException.java,v 1.1 2006/03/09 06:38:19 gregluck Exp $
 */
public class CacheException extends RuntimeException {

    /** Constructor for the CacheException object */
    public CacheException() {
        super();
    }

    /**
     * Constructor for the CacheException object
     *
     * @param message
     */
    public CacheException(String message) {
        super(message);
    }

}
