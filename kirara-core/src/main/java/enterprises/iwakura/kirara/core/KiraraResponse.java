package enterprises.iwakura.kirara.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Handy implementation of {@link SupportsKiraraResponse} that provides a Kirara instance.
 * This class is typically used as a base class for response objects that need to be associated with
 * a Kirara instance.
 *
 * @param <T> The type of Kirara instance associated with this response.
 */
@Getter
@Setter
public abstract class KiraraResponse<T extends Kirara> implements SupportsKiraraResponse<T> {

    /**
     * The Kirara instance associated with this response. Is marked with transient to avoid deserialization.
     */
    protected transient T kirara;

    /**
     * Constructor
     */
    public KiraraResponse() {
    }
}
