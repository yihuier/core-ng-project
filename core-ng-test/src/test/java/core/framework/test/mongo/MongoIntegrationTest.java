package core.framework.test.mongo;

import com.mongodb.client.model.Filters;
import core.framework.api.mongo.MongoCollection;
import core.framework.test.IntegrationTest;
import org.bson.types.ObjectId;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class MongoIntegrationTest extends IntegrationTest {
    @Inject
    MongoCollection<TestEntity> collection;

    @Test
    public void insert() {
        TestEntity entity = new TestEntity();
        entity.stringField = "string";
        collection.insert(entity);

        assertNotNull(entity.id);

        Optional<TestEntity> loadedEntity = collection.get(entity.id);
        assertTrue(loadedEntity.isPresent());
        assertEquals(entity.stringField, loadedEntity.get().stringField);
    }

    @Test
    public void replace() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        entity.stringField = "value1";
        collection.replace(entity);

        TestEntity loadedEntity = collection.get(entity.id).get();
        assertEquals(entity.stringField, loadedEntity.stringField);

        entity.stringField = "value2";
        collection.replace(entity);

        loadedEntity = collection.get(entity.id).get();
        assertEquals(entity.stringField, loadedEntity.stringField);
    }

    @Test
    public void search() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        entity.stringField = "value";
        collection.insert(entity);

        List<TestEntity> entities = collection.find(Filters.eq("string_field", "value"));
        assertEquals(1, entities.size());
        assertEquals(entity.id, entities.get(0).id);
        assertEquals(entity.stringField, entities.get(0).stringField);
    }
}
