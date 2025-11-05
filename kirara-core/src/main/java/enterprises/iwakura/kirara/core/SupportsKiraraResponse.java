package enterprises.iwakura.kirara.core;

/**
 * Interface for classes that support a Kirara instance.
 * This is typically used to associate a Kirara instance with a response object.
 *
 * @param <T> The type of Kirara instance.
 */
public interface SupportsKiraraResponse<T extends Kirara> {

    /**
     * Sets the Kirara instance for this response.
     *
     * @param kirara The Kirara instance to set.
     */
    void setKirara(T kirara);

    /**
     * Gets the Kirara instance associated with this response.
     *
     * @return The Kirara instance.
     */
    T getKirara();

}
