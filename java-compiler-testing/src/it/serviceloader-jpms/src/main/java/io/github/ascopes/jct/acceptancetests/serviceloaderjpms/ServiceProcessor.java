/*
 * Copyright (C) 2022 - 2025, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ascopes.jct.acceptancetests.serviceloaderjpms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;

/**
 * Annotation processor that creates {@code META-INF/services} files from types that are annotated
 * with {@link Service}.
 *
 * @author Ashley Scopes
 */
public class ServiceProcessor extends AbstractProcessor {

  public ServiceProcessor() {
    // Hope you are happy, javac. I made a constructor for you.
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(Service.class.getName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(
      Set<? extends TypeElement> annotations,
      RoundEnvironment roundEnv
  ) {
    annotations
        .stream()
        .map(roundEnv::getElementsAnnotatedWith)
        .flatMap(Set::stream)
        .map(TypeElement.class::cast)
        .map(this::createAnnotatedService)
        .collect(Collectors.groupingBy(AnnotatedService::getInterfaceType))
        .forEach(this::createServiceFile);

    return false;
  }

  private AnnotatedService createAnnotatedService(TypeElement type) {
    final var annotationMirror = type
        .getAnnotationMirrors()
        .stream()
        .filter(this::isServiceAnnotation)
        .findFirst()
        .orElseThrow();

    final var interfaceType = annotationMirror
        .getElementValues()
        .values()
        .stream()
        .findFirst()
        .map(AnnotationValue::getValue)
        .map(TypeMirror.class::cast)
        .map(processingEnv.getTypeUtils()::asElement)
        .map(TypeElement.class::cast)
        .map(TypeElement::getQualifiedName)
        .map(Name::toString)
        .orElseThrow();

    final var implementationType = type.getQualifiedName().toString();

    processingEnv
        .getMessager()
        .printMessage(
            Kind.NOTE,
            String.format(
                "Found %s which implements service interface %s",
                implementationType,
                interfaceType
            ),
            type,
            annotationMirror
        );

    return new AnnotatedService(interfaceType, implementationType);
  }

  private boolean isServiceAnnotation(AnnotationMirror annotationMirror) {
    var annotationType = (TypeElement) annotationMirror.getAnnotationType().asElement();
    return annotationType.getQualifiedName().contentEquals(Service.class.getName());
  }

  private void createServiceFile(String interfaceType, List<AnnotatedService> services) {
    try {
      var resource = processingEnv
          .getFiler()
          .createResource(
              StandardLocation.CLASS_OUTPUT,
              "",
              "META-INF/services/" + interfaceType
          );

      try (var writer = new BufferedWriter(resource.openWriter())) {
        for (var service : services) {
          writer.write(service.getImplementationType());
          writer.write('\n');
        }
      }
    } catch (IOException ex) {
      var stackTrace = new StringWriter();
      ex.printStackTrace(new PrintWriter(stackTrace));

      processingEnv
          .getMessager()
          .printMessage(Kind.ERROR, stackTrace.toString());
    }
  }
}
