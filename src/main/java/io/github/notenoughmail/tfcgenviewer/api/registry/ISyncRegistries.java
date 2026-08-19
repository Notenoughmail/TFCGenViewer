package io.github.notenoughmail.tfcgenviewer.api.registry;

import io.github.notenoughmail.tfcgenviewer.api.SerializationInformation;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;

/**
 * A registry syncer requests a server-only registry's contents (or subsets thereof)
 * to be synced and provides information on how the registry's contents are serialized
 */
public interface ISyncRegistries {

    /**
     * Request the server-only registries to be synced to the client
     */
    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {}

    /**
     * Provide the {@link com.mojang.serialization.Codec Codec} used to serialize
     * the contents of a server-only registry
     */
    default void elementCodecForRegistry(SerializationInformation serializationInformation) {}
}
