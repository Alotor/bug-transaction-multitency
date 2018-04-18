package bug

import org.grails.datastore.gorm.jdbc.schema.DefaultSchemaHandler
import javax.sql.DataSource
import java.sql.Connection
import java.sql.ResultSet

class CustomSchemaHandler extends DefaultSchemaHandler {
    @Override
    void useSchema(Connection connection, String name) {
        def schemaNames = []

        ResultSet schemas = connection.metaData.schemas
        while (schemas.next()) {
            schemaNames.add(schemas.getString("TABLE_SCHEM"))
        }

        if (!schemaNames*.toUpperCase().contains(name.toUpperCase())) {
            throw new RuntimeException("Schema ${name} doesn't exist")
        }

        connection.createStatement()
                  .execute(String.format("SET SEARCH_PATH TO %s, public", name))
    }

    @Override
    void createSchema(Connection connection, String name) {
        if (name.toUpperCase().equals("INFORMATION_SCHEMA") ||
            name.toUpperCase().equals("PUBLIC") ||
            name.toUpperCase().startsWith("PG_")) {
            // Don't create pre-existing schemas
            return
        }
        super.createSchema(connection, name)
    }

    @Override
    Collection<String> resolveSchemaNames(DataSource dataSource) {
        def result = super.resolveSchemaNames(dataSource).findAll {
            it.toUpperCase() != 'PUBLIC' &&
            it.toUpperCase() != 'INFORMATION_SCHEMA' &&
            !it.toUpperCase().startsWith('PG_')
        }
        return result
    }
}
