/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.meta.TypeQualifierDefault;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Marker annotation for a package.
 *
 * <p>Any package annotated with this will assume all methods, parameters, and type parameters
 * in this API are non-null unless explicitly specified with {@link Nullable}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
@Documented
@Inherited
@Nonnull
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.PACKAGE,
    ElementType.TYPE,
})
@TypeQualifierDefault({
    ElementType.METHOD,
    ElementType.PARAMETER,
    ElementType.TYPE_PARAMETER,
})
public @interface NonNullApi {
}
