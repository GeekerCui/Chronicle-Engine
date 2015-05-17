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

package net.openhft.chronicle.engine.map;

import net.openhft.chronicle.wire.ValueIn;
import net.openhft.chronicle.wire.ValueOut;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;

import java.io.StreamCorruptedException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;


public interface MapWireHandler<K, V> {

    void process(@NotNull Wire in,
                 @NotNull Wire out,
                 @NotNull Map<K, V> map,
                 @NotNull CharSequence csp,
                 long tid,
                 @NotNull BiConsumer<ValueOut, V> vToWire,
                 @NotNull Function<ValueIn, K> kFromWire,
                 @NotNull Function<ValueIn, V> vFromWire) throws StreamCorruptedException;

}
