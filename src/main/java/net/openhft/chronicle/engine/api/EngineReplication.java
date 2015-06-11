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

package net.openhft.chronicle.engine.api;

import net.openhft.chronicle.bytes.Bytes;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * @author Rob Austin.
 */
interface EngineReplication extends Closeable {

    /**
     * inserts or updates the entry
     *
     * @param key              the key of the entry
     * @param value            the value of the entry
     * @param remoteIdentifier the identifier of the remote server
     * @param timestamp        the timestamp send from the remote server, this time stamp was the time the entry was removed
     */
    void put(Bytes key, Bytes value, byte remoteIdentifier, long timestamp);

    /**
     * marks the entry as removed
     *
     * @param key              the key of the entry
     * @param remoteIdentifier the identifier of the remote server
     * @param timestamp        the timestamp send from the remote server, this time stamp was the time the entry was removed
     */
    void remove(Bytes key, Bytes remoteIdentifier, long timestamp);

    /**
     * Provides the unique Identifier associated with this instance. <p> An identifier is used
     * to determine which replicating node made the change. <p> If two nodes update their map at the
     * same time with different values, we have to deterministically resolve which update wins,
     * because of eventual consistency both nodes should end up locally holding the same data.
     * Although it is rare two remote nodes could receive an update to their maps at exactly the
     * same time for the same key, we have to handle this edge case, its therefore important not to
     * rely on timestamps alone to reconcile the updates. Typically the update with the newest
     * timestamp should win,  but in this example both timestamps are the same, and the decision
     * made to one node should be identical to the decision made to the other. We resolve this
     * simple dilemma by using a node identifier, each node will have a unique identifier, the
     * update from the node with the smallest identifier wins.
     *
     * @return the unique Identifier associated with this map instance
     */
    byte identifier();

    /**
     * Gets (if it does not exist, creates) an instance of ModificationIterator associated with a
     * remote node, this weak associated is bound using the {@code identifier}.
     *
     * @param remoteIdentifier the identifier of the remote node
     * @return the ModificationIterator dedicated for replication to the remote node with the given
     * identifier
     * @see #identifier()
     */
    ModificationIterator acquireModificationIterator(byte remoteIdentifier);

    /**
     * Returns the timestamp of the last change from the specified remote node, already replicated
     * to this host.  <p>Used in conjunction with replication, to back fill data from a remote
     * node. This node may have missed updates while it was not been running or connected via TCP.
     *
     * @param remoteIdentifier the identifier of the remote node to check last replicated update
     *                         time from
     * @return a timestamp of the last modification to an entry, or 0 if there are no entries.
     * @see #identifier()
     */
    long lastModificationTime(byte remoteIdentifier);

    void setLastModificationTime(byte identifier, long timestamp);

    /**
     * notifies when there is a changed to the modification iterator
     */
    interface ModificationNotifier {
        ModificationNotifier NOP = new ModificationNotifier() {
            @Override
            public void onChange() {
            }
        };

        /**
         * called when ever there is a change applied to the modification iterator, in otherwords when
         * {@link net.openhft.chronicle.engine.api.EngineReplication.ModificationIterator#hasNext()} will return true
         */
        void onChange();
    }

    /**
     * Holds a record of which entries have modification. Each remote map supported will require a
     * corresponding ModificationIterator instance
     */
    interface ModificationIterator {

        /**
         * @return {@code true} if the is another entry to be received via {@link
         * ModificationIterator#nextEntry(net.openhft.chronicle.engine.api.EngineReplication.EntryCallback)}
         */
        boolean hasNext();

        /**
         * A non-blocking call that provides the entry that has changed to {@code
         * callback.onEntry()}.
         *
         * @param callback a callback which will be called when a new entry becomes available.
         * @return {@code true} if the entry was accepted by the {@code callback.onEntry()} method,
         * {@code false} if the entry was not accepted or was not available
         */
        boolean nextEntry(@NotNull final EntryCallback callback) throws InterruptedException;

        /**
         * Dirties all entries with a modification time equal to {@code fromTimeStamp} or newer. It
         * means all these entries will be considered as "new" by this ModificationIterator and
         * iterated once again no matter if they have already been.  <p>This functionality is used
         * to publish recently modified entries to a new remote node as it connects.
         *
         * @param fromTimeStamp the timestamp from which all entries should be dirty
         */
        void dirtyEntries(long fromTimeStamp) throws InterruptedException;

        /**
         * the {@code modificationNotifier} is called when ever there is a change applied to the
         * modification iterator
         *
         * @param modificationNotifier gets notified when a change occurs
         */
        void setModificationNotifier(@NotNull final ModificationNotifier modificationNotifier);
    }

    /**
     * Implemented typically by a replicator, This interface provides the event, which will get
     * called whenever a put() or remove() has occurred to the map
     */
    abstract class EntryCallback {

        /**
         * Called whenever a put() or remove() has occurred to a replicating map.
         *
         * @param key
         * @param value
         * @param bootStrapTimeStamp sent to the client on every update this is the timestamp
         *                           that the remote client should bootstrap from when there has
         *                           been a disconnection, this time maybe later than the message
         *                           time as event are not send in chronological order from the
         *                           bit set.
         * @return {@code false} if this entry should be ignored because the identifier of the
         * source node is not from one of our changes, WARNING even though we check the identifier
         * in the ModificationIterator the entry may have been updated.
         */
        public abstract boolean onEntry(@NotNull Bytes key,
                                        @NotNull Bytes value,
                                        long timestamp,
                                        byte identifier,
                                        long bootStrapTimeStamp);
    }
}
