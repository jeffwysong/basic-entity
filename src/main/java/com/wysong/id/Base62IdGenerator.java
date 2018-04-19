package com.wysong.id;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * An {@link IdGenerator} that generates Base62 Unique Identifiers that are based on TimeUUIDs.
 * <p/>
 * These UIDs are different from standard Hex-encoded UUIDs because they are:
 * <ol>
 *     <li>Shorter in length, requiring less database storage</li>
 *     <li>CaSe SeNSiTiVe</li>
 * </ol>
 * Because they are case sensitive, if they are to be stored in a database, that database must support case-insensitive
 * queries to lookup records by one of these IDs.  This isn't a problem for the vast majority of databases, but it is
 * unknown how Microsoft SQL Server supports such behavior for example.
 *
 */
@Component
public class Base62IdGenerator implements IdGenerator {
    @Override
    public String generateId() {
        UUID uuid = createTimeUUID();
        return Base62Codec.encode(uuid);
    }

    /**
     * Static method to create a new Base62 time based UUID.
     *
     * @return a Base62 encoded string of a time based UUID.
     */
    public static String getId() {
        UUID uuid = createTimeUUID();
        return Base62Codec.encode(uuid);
    }

    /**
     * Gets a new time uuid.
     *
     * @return the time uuid
     */
    private static UUID createTimeUUID() {
        return UUID.fromString(new com.eaio.uuid.UUID().toString());
    }

}
