/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.collection.model;

import java.util.Comparator;

public class NaturalComparator<E extends Comparable<? super E>> implements Comparator<E> {


    /**
     * The singleton instance.
     */
    private static final NaturalComparator<?> INSTANCE = new NaturalComparator<>();

    //-----------------------------------------------------------------------

    /**
     * Constructor whose use should be avoided.
     * <p>
     * Please use the {@link #getInstance()} method whenever possible.
     */
    public NaturalComparator() {
        super();
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the singleton instance of a ComparableComparator.
     * <p>
     * Developers are encouraged to use the comparator returned from this method
     * instead of constructing a new instance to reduce allocation and GC overhead
     * when multiple comparable comparators may be used in the same VM.
     *
     * @param <E> the element type
     * @return the singleton ComparableComparator
     */
    public static <E extends Comparable<? super E>> NaturalComparator<E> getInstance() {
        return (NaturalComparator<E>) INSTANCE;
    }

    //-----------------------------------------------------------------------

    /**
     * Compare the two {@link Comparable Comparable} arguments.
     * This method is equivalent to:
     * <pre>((Comparable)obj1).compareTo(obj2)</pre>
     *
     * @param a the first object to compare
     * @param b the second object to compare
     * @return negative if obj1 is less, positive if greater, zero if equal
     * @throws NullPointerException if <i>obj1</i> is <code>null</code>,
     *                              or when <code>((Comparable)obj1).compareTo(obj2)</code> does
     * @throws ClassCastException   if <i>obj1</i> is not a <code>Comparable</code>,
     *                              or when <code>((Comparable)obj1).compareTo(obj2)</code> does
     */
    @Override
    public int compare(final E a, final E b) {
        return a.compareTo(b);
    }

}
