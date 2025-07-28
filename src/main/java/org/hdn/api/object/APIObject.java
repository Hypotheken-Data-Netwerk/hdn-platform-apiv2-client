package org.hdn.api.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * The base object of all API objects
 */
public class APIObject {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Returns a String representation of the APIObject
     * @return a String representation
     */
    @SuppressWarnings("java:S3011") // Toestaan van reflectieve toegang tot private velden in toString()
    @Override
    public String toString() {
        Field[] fields = this.getClass().getDeclaredFields();
        StringBuilder object = new StringBuilder();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                object.append(String.format("%s: %s, ", field.getName(), field.get(this)));
            } catch (IllegalAccessException e) {
                logger.error("{}", e.getMessage());
            }
        }
        return object.substring(0, object.length() - 1);
    }
}
