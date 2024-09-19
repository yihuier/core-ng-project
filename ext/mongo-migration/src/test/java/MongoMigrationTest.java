import core.ext.mongo.migration.MongoMigration;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @author Neal
 */
class MongoMigrationTest {
    @Test
    @Tag("integration")
    void testMigration() {
        var uri = "mongodb://localhost:27017/recipe"; //test mongoURI ?
        MongoMigration migration = new MongoMigration(uri);
        migration.scanPackagePath("core.ext.mongo.test.script");
        migration.migration();
    }

    @Test
    @Tag("integration")
    void testExecute() {
        var uri = "mongodb://localhost:27017/admin"; //test mongoURI ?
        MongoMigration migration = new MongoMigration(uri);
        migration.execute(mongoDatabase -> {
            mongoDatabase.runCommand(new Document().append("setParameter", 1).append("notablescan", 1));
        });
    }
}
