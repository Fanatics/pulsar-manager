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

import io.swagger.annotations.Api;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.manager.entity.UserInfoEntity;
import org.apache.pulsar.manager.entity.UsersRepository;
import org.apache.pulsar.manager.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Api("Support user login and logout using okta")
@RestController
public class FanaticsLoginController {

  private final JwtService jwtService;
  private final UsersRepository usersRepository;

  public FanaticsLoginController(JwtService jwtService, UsersRepository usersRepository) {
    this.usersRepository = usersRepository;
    this.jwtService = jwtService;
  }

  @RequestMapping(value = "/fpb-manager/okta", method = RequestMethod.GET)
  public ResponseEntity<String> login(HttpServletResponse response) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = "";
    if (principal instanceof UserDetails) {
      username = ((UserDetails)principal).getUsername();
    } else {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    HttpServletRequest request =  ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    Optional<UserInfoEntity> userInfoEntityOptional = usersRepository.findByUserName(username);
    UserInfoEntity userInfoEntity = new UserInfoEntity();
    if (!userInfoEntityOptional.isPresent()) {
      userInfoEntity.setUserId(0);
      userInfoEntity.setName(username);
      userInfoEntity.setPassword("");
      userInfoEntity.setExpire(0);
      usersRepository.save(userInfoEntity);
    } else {
      userInfoEntity = userInfoEntityOptional.get();
    }
    String token = jwtService.toToken(username);
    userInfoEntity.setAccessToken(token);
    usersRepository.update(userInfoEntity);
    jwtService.setToken(request.getSession().getId(), token);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_HTML);
    String res = String.join("\n",//
            "<html><head><script>", //
            "localStorage.setItem('" + "username" + "', '" + username + "');", //
            "localStorage.setItem('" + "Admin-Token" + "', '" + token + "');", //
            "var defaultUrl = '/ui/index.html'",
            "window.location.href = defaultUrl", //
            "</script></head></html>"//
    );

    return new ResponseEntity<>(res, headers, HttpStatus.OK);
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public void home(HttpServletResponse response) {
    response.addHeader("Location", "/fpb-manager/okta");
    response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
  }
}
