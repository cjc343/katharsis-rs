package io.katharsis.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.rs.controller.KatharsisController;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * Basic Katharsis feature that initializes core classes and provides a starting point to use the framework in
 * another projects.
 *
 * This feature has NO {@link Provider} annotation, thus it require to provide an instance of  {@link ObjectMapper} and
 * {@link JsonServiceLocator} to provide instances of resources.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class KatharsisFeature implements Feature {

    private final JsonServiceLocator jsonServiceLocator;
    private final ObjectMapper objectMapper;

    public KatharsisFeature(ObjectMapper objectMapper, JsonServiceLocator jsonServiceLocator) {
        this.objectMapper = objectMapper;
        this.jsonServiceLocator = jsonServiceLocator;
    }

    @Override
    public boolean configure(FeatureContext context) {
        String resourceSearchPackage = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);
        String resourceDefaultDomain = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN);

        KatharsisController katharsisController = createJsonApiController(resourceSearchPackage, resourceDefaultDomain);
        context.register(katharsisController);

        return true;
    }

    private KatharsisController createJsonApiController(String resourceSearchPackage, String resourceDefaultDomain) {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(jsonServiceLocator, new ResourceInformationBuilder());
        ResourceRegistry resourceRegistry = registryBuilder.build(resourceSearchPackage, resourceDefaultDomain);

        RequestDispatcher requestDispatcher = createRequestDispatcher(resourceRegistry);

        return new KatharsisController(objectMapper, resourceRegistry, requestDispatcher);
    }

    private RequestDispatcher createRequestDispatcher(ResourceRegistry resourceRegistry) {
        ControllerRegistryBuilder controllerRegistryBuilder = new ControllerRegistryBuilder();
        ControllerRegistry controllerRegistry = controllerRegistryBuilder
                .build(resourceRegistry);
        return new RequestDispatcher(controllerRegistry);
    }
}
