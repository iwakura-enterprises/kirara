package enterprises.iwakura.kirara.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Handy implementation of {@link SupportsKiraraResponse} that provides a Kirara instance.
 * This class is typically used as a base class for response objects that need to be associated with
 * a Kirara instance.
 */
@Getter
@Setter
public abstract class KiraraResponse implements SupportsKiraraResponse {

    protected Kirara kirara;
}
