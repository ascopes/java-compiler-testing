/*
 * Copyright (C) 2022 Ashley Scopes
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

/**
 * Implementation of a JSR-199-compliant Path {@link javax.tools.JavaFileManager}
 * which supports modules, multiple paths per {@link javax.tools.JavaFileManager.Location},
 * and the ability to use in-memory file systems provided by {@link com.google.common.jimfs.Jimfs}.
 */
package com.github.ascopes.jct.paths;