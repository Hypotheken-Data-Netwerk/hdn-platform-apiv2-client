package nl.hdn.api.object;

import nl.hdn.api.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.security.InvalidParameterException;

/**
 * The base object of all API objects
 */
public class APIObject {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected void validateOnBehalfOf(String onBehalfOf) throws InvalidParameterException {
        if (ConfigUtils.getBoolean(ConfigUtils.SKIP_ONBEHALFOF_VALIDATION, false)) {
            logger.info("Skipping onBehalfOf validation ({}=true).", ConfigUtils.SKIP_ONBEHALFOF_VALIDATION);
            return;
        }

        if (onBehalfOf == null || !onBehalfOf.matches("\\d{6}")) {
            logger.error("onBehalfOf node is not set or doesn't match 6 digits but required");
            throw new InvalidParameterException("onBehalfOf is required");
        }
    }

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
