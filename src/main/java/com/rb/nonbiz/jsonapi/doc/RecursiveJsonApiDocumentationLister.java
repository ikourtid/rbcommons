package com.rb.nonbiz.jsonapi.doc;

import com.rb.nonbiz.jsonapi.JsonApiArrayDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiClassDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiClassWithNonFixedPropertiesDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiClassWithSubclassesDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiDocumentation.Visitor;
import com.rb.nonbiz.jsonapi.JsonApiEnumDocumentation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBOptionals.optionalToStream;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentation.getAllJsonApiDocumentation;

/**
 * Creates a list of (possibly non-unique) {@link JsonApiDocumentation} by recursively traversing a root
 * {@link JsonApiDocumentation} and its children, etc. For example, the {@link JsonApiDocumentation} for
 * MarketInfo will also have {@link JsonApiDocumentation} for CurrentMarketInfo
 * DailyMarketInfo, and so on.
 */
public class RecursiveJsonApiDocumentationLister {

  public List<JsonApiDocumentation> list(JsonApiDocumentation root) {
    return listAsStream(root).collect(Collectors.toList());
  }

  private Stream<JsonApiDocumentation> listAsStream(JsonApiDocumentation root) {
    // This method allows #listChildrenAsStream to be a bit simpler, because we always want to include the root
    // object regardless of JsonApiDocumentation subclass.
    return Stream.concat(
        Stream.of(root),
        listChildrenAsStream(root));
  }

  private Stream<JsonApiDocumentation> listChildrenAsStream(JsonApiDocumentation root) {
    return root.visit(new Visitor<Stream<JsonApiDocumentation>>() {
      @Override
      public Stream<JsonApiDocumentation> visitJsonApiClassDocumentation(
          JsonApiClassDocumentation jsonApiClassDocumentation) {
        return jsonApiClassDocumentation.getChildJsonApiConverters()
            .stream()
            // Intentional recursion here, so we can enumerate all JSON API converter verb classes that implement
            // HasJsonApiDocumentation, and fetch their JsonApiDocumentation.
            .flatMap(hasJsonApiDocumentation -> getAllJsonApiDocumentation(hasJsonApiDocumentation)
                .stream()
                .flatMap(v -> listAsStream(v)));
      }

      @Override
      public Stream<JsonApiDocumentation> visitJsonApiEnumDocumentation(
          JsonApiEnumDocumentation<? extends Enum<?>> jsonApiEnumDocumentation) {
        // In the case of an enum, there are no properties whose types we also need to look up documentation for.
        // The enum is just a primitive.
        return Stream.empty();
      }

      @Override
      public Stream<JsonApiDocumentation> visitJsonApiClassWithSubclassesDocumentation(
          JsonApiClassWithSubclassesDocumentation jsonApiClassWithSubclassesDocumentation) {
        return jsonApiClassWithSubclassesDocumentation.getJsonApiSubclassInfoList()
            .stream()
            // Trickery to convert Optional<HasJsonApiDocumentation> to a Stream<HasJsonApiDocumentation>,
            // where the stream will have a size of either 0 (if optional is empty) or 1 (if not)
            .flatMap(v -> optionalToStream(v.getJsonApiConverterForTraversing()))
            .flatMap(v -> getAllJsonApiDocumentation(v).stream())
            .flatMap(v -> listAsStream(v));
      }

      @Override
      public Stream<JsonApiDocumentation> visitJsonApiArrayDocumentation(
          JsonApiArrayDocumentation jsonApiArrayDocumentation) {
        return transformOptional(
            jsonApiArrayDocumentation.getChildJsonApiConverter(),
            v -> getAllJsonApiDocumentation(v)
                .stream()
                .flatMap(v2 -> listAsStream(v2)))
            .orElse(Stream.empty());
      }

      @Override
      public Stream<JsonApiDocumentation> visitJsonApiClassWithNonFixedPropertiesDocumentation(
          JsonApiClassWithNonFixedPropertiesDocumentation jsonApiClassWithNonFixedPropertiesDocumentation) {
        return jsonApiClassWithNonFixedPropertiesDocumentation.getChildJsonApiConverters()
            .stream()
            // Intentional recursion here, so we can enumerate all JSON API converter verb classes that implement
            // HasJsonApiDocumentation, and fetch their JsonApiDocumentation.
            .flatMap(hasJsonApiDocumentation -> getAllJsonApiDocumentation(hasJsonApiDocumentation)
                .stream()
                .flatMap(v -> listAsStream(v)));
      }
    });
  }

}
