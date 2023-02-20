/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pulsar.manager.fanatics.controller;

import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/fpb-manager")
@Api("Support user login and logout using okta")
@RestController
public class FanaticsLoginController {
  @RequestMapping(value = "/okta", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> login() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("toke", "generate-token");
    headers.add("role", "super");
    headers.add("username", "pgarule");

    Map<String, Object> result = Maps.newHashMap();
    result.put("login", "success");
    System.out.println("------------------------ Okta login success --------------------------");
    return new ResponseEntity<>(result, headers, HttpStatus.OK);
  }
}
