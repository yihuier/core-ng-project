package core.framework.internal.cache;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.validate.ClassValidator;

/**
 * @author neo
 */
final class CacheClassValidator {
    private final ClassValidator validator;

    CacheClassValidator(Class<?> cacheClass) {
        // cache class validator accepts all json types without @Property annotation checking
        validator = JSONClassValidator.classValidator(cacheClass);
    }

    public void validate() {
        validator.validate();
    }
}
