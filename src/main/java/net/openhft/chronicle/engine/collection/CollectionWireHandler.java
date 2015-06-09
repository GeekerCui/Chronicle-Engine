/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.engine.collection;

import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;

import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.openhft.chronicle.engine.collection.CollectionWireHandler.Params.key;
import static net.openhft.chronicle.engine.collection.CollectionWireHandler.Params.segment;

/**
 * @param <U> the type of each element in that collection
 */
public interface CollectionWireHandler<U, C extends Collection<U>> {

    void process(Wire in,
                 Wire out,
                 C collection,
                 CharSequence csp,
                 BiConsumer<ValueOut, U> toWire,
                 Function<ValueIn, U> fromWire,
                 Supplier<C> factory,
                 long tid) throws StreamCorruptedException;

    enum Params implements WireKey {
        key,
        segment,
    }

    enum SetEventId implements ParameterizeWireKey {
        size,
        isEmpty,
        add,
        addAll,
        retainAll,
        containsAll,
        removeAll,
        clear,
        remove(key),
        numberOfSegments,
        contains(key),
        iterator(segment);

        private final WireKey[] params;

        <P extends WireKey> SetEventId(P... params) {
            this.params = params;
        }

        @NotNull
        public <P extends WireKey> P[] params() {
            return (P[]) this.params;
        }
    }
}
