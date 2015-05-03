package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.utils.Generics;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public class ResourceDelete implements BaseController {

    private ResourceRegistry resourceRegistry;

    public ResourceDelete(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * Checks if requested resource method is acceptable - is a DELETE request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof ResourcePath
                && "DELETE".equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String resourceName = jsonPath.getElementName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException("Resource of type not found: " + resourceName);
        }
        for (String id : resourceIds.getIds()) {
            Class<?> idType = registryEntry.getResourceInformation().getIdField().getType();
            Serializable castedId = Generics.castIdValue(id, idType);
            registryEntry.getResourceRepository().delete(castedId);
        }

        return null;
    }
}