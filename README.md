# Sample strange behaviour transactional service

The main issue is that a `RuntimeException` is not rolling-back the transaction openend in
a service when a previous access has been done in the controller.

This project is set-up with multitenancy and postgres, we think it might be related with the
multitenancy.

On this repository we have the following artifacts:

## Test.groovy 

A sample domain mapped to a tenant

```
class Test implements MultiTenant<Test> {
    String name
    String description
    Date dateCreated

    static constraints = {
        description(nullable: true)
    }

    static mapping = {
        version false
    }
}
```

## TestController.groovy 

The basic entry point. Is marked with @CurrentTenant. On the body we're accessing a domain with a Test.get(*)

```
@CurrentTenant
class TestController {
    TestService testService

    def create() {
        // Access to the session before the service
        def value = Test.get(1)
        println(">>> " + value)

        // Execute the "transactional" service
        testService.execute()
        render([:] as JSON)
    }
}
```

## TestService.groovy 

Marked with @Transaction we save several instances and then throw an exception

```
@Transactional
class TestService {
    void execute() {
        new Test(name: "one", description: "one").save(flush: true)
        new Test(name: "one", description: "one").save(flush: true)
        new Test(name: "one", description: "one").save(flush: true)
        new Test(name: "one", description: "one").save(flush: true)
        new Test(name: "one", description: "one").save(flush: true)

        // We expect that the previous data is not stored!
        // But it is
        throw new RuntimeException("Failing")
    }
}
```

## Multitenancy - Tenant Resolver

```
grails:
    gorm:
        multiTenancy:
            mode: SCHEMA
            tenantResolverClass: bug.CustomTenantResolver
            defaultTenant: _default
```

This Tenant Resolver only returns the value stored in the value `defaultTenant`

```
class CustomTenantResolver implements TenantResolver {
    @Override
    Serializable resolveTenantIdentifier() {
        Holders.config['grails.gorm.multiTenancy.defaultTenant']
    }
}
```

## Multitenancy - Schema handler

We have defined a custom scheman handler to support multitenancy with schemas:

```
dataSource:
  pooled: true
  jmxExport: true
  driverClassName: org.postgresql.Driver
  dialect: org.hibernate.dialect.PostgreSQL94Dialect
  schemaHandler: bug.CustomSchemaHandler
  username: bugtest
  password: bugtest
```

This schema handler basicaly uses the statement `set search_path to X` to add to the public schema the one defined for the current tenant.

```
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
```

## Setting up the Database

The test is prepared to with PostgresSQL in mind. If you want to set up the database run in localhost PostgreSQL and run the script `createdb.sh`

