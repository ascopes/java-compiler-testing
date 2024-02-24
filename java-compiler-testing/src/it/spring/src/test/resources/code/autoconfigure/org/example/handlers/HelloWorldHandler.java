/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package org.example.handlers;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler that just says hello and then finishes. Very enterprise-oriented logic.
 */
@Component
public class HelloWorldHandler {

  /**
   * Just say the message and then go away.
   *
   * @param request the GET request.
   * @return a 200 OK response that very politely greets me and makes me feel like I have an AI as a
   *     friend.
   */
  public Mono<ServerResponse> getHelloWorld(ServerRequest request) {
    return ServerResponse
        .ok()
        .bodyValue("Hello, World!");
  }
}
