package bug

import grails.util.Holders
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.multitenancy.TenantResolver

@Slf4j
class CustomTenantResolver implements TenantResolver {
    @Override
    Serializable resolveTenantIdentifier() {
        Holders.config['grails.gorm.multiTenancy.defaultTenant']
    }
}

