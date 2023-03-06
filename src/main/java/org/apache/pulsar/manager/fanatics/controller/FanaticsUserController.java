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
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.pulsar.manager.entity.UserInfoEntity;
import org.apache.pulsar.manager.entity.UsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(value = "/pulsar-manager")
@Api(description = "Functions under this class are available to super user.")
public class FanaticsUserController {
    private final UsersRepository usersRepository;

    private final String platforms_group = "Application Platforms Team";
    public FanaticsUserController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }
    @ApiOperation(value = "Get user info")
    @ApiResponses({
            @ApiResponse(code = 200, message = "ok"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @RequestMapping(value = "/users/userInfo", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Map<String, Object> result = Maps.newHashMap();
        Set<String> roles  = Sets.newHashSet();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        Optional<UserInfoEntity> userInfoEntityOptional = usersRepository.findByAccessToken(token);
        if (!userInfoEntityOptional.isPresent()) {
            result.put("error", "User is no exist");
            return ResponseEntity.ok(result);
        }
        UserInfoEntity userInfoEntity = userInfoEntityOptional.get();
        result.put("message", "Get user info success");
        result.put("userName", userInfoEntity.getName());
        result.put("description", userInfoEntity.getDescription());

        SecurityContextHolder.getContext().getAuthentication().getAuthorities().forEach(grantedAuthority -> {
            if (grantedAuthority.getAuthority().equals(platforms_group)) {
                roles.add("super");
            }
        });

        roles.add("admin");
        result.put("roles", roles);
        return ResponseEntity.ok(result);
    }
}
