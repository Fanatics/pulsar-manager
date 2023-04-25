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
package org.apache.pulsar.manager.zuul;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.manager.fanatics.argos.FanaticsArgosLogger;
import org.apache.pulsar.manager.fanatics.dcs.UserTopicPermissionReader;
import org.apache.pulsar.manager.fanatics.utils.UserUtils;
import org.apache.pulsar.manager.service.EnvironmentCacheService;
import org.apache.pulsar.manager.service.PulsarAdminService;
import org.apache.pulsar.manager.service.PulsarEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.REQUEST_URI_KEY;

/**
 * Handle http redirect and forward.
 */
@Component
@Slf4j
public class EnvironmentForward extends ZuulFilter {

    private final EnvironmentCacheService environmentCacheService;

    private final PulsarEvent pulsarEvent;

    private final PulsarAdminService pulsarAdminService;

    private final UserTopicPermissionReader userTopicPermissionReader;

    @Autowired
    public EnvironmentForward(
            EnvironmentCacheService environmentCacheService, PulsarEvent pulsarEvent,
            PulsarAdminService pulsarAdminService, UserTopicPermissionReader userTopicPermissionReader) {
        this.environmentCacheService = environmentCacheService;
        this.pulsarEvent = pulsarEvent;
        this.pulsarAdminService = pulsarAdminService;
        this.userTopicPermissionReader = userTopicPermissionReader;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SEND_FORWARD_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @SneakyThrows
    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String redirect = request.getParameter("redirect");

        String requestUri = request.getServletPath();
        String username = request.getHeader("username");

        // only support get requests for broker
        if (!request.getMethod().equals("GET")) {
            // for normal users CUD are permitted only on dev
            if(!System.getenv("SHORT_ENV_GROUP").equals("dev") && !UserUtils.isSuperUser()) {
                return null;
            }

            // check if it is clear backlog call
            if (requestUri.endsWith("skip_all")) {
                String path = requestUri;
                path = path.replace("/admin/v2/persistent/", "").replace("/skip_all", "");
                String[] pathSplit = path.split("/subscription/");
                if (pathSplit.length == 2) {
                    if(!UserUtils.isSuperUser() && !userTopicPermissionReader.hasClearBacklogPermission(username, pathSplit[0], pathSplit[1])) {
                        return null;
                    }
                    String logMessage = String.format("Request: clear_backlog, user: %s, topic: %s, subscription: %s", username, pathSplit[0], pathSplit[1]);
                    log.info(logMessage);
                    FanaticsArgosLogger.logInfo(logMessage, username);
                } else {
                    return null;
                }
            } else if (requestUri.endsWith("schema")) {
                String topic = requestUri.replace("/admin/v2/schemas/", "").replace("/schema", "");
                if (!UserUtils.isSuperUser() && !userTopicPermissionReader.hasDeleteSchemaPermission(username, topic)) {
                   return null;
                }
                String logMessage = String.format("Request: delete_schema, user: %s, topic: %s", username, topic);
                log.info(logMessage);
                FanaticsArgosLogger.logInfo(logMessage, username);
            }
            else if (!UserUtils.isSuperUser()) {
                return null;
            }
        }

        if (redirect != null && redirect.equals("true")) {
            String redirectScheme = request.getParameter("redirect.scheme");
            String redirectHost = request.getParameter("redirect.host");
            String redirectPort = request.getParameter("redirect.port");
            String url = redirectScheme + "://" + redirectHost + ":" + redirectPort;
            return forwardRequest(ctx, request, url);
        }

        String broker = request.getHeader("x-pulsar-broker");
        if (StringUtils.isNotBlank(broker)) { // the request should be forward to a pulsar broker
            // TODO: support https://
            String serviceUrl = "http://" + broker;
            return forwardRequest(ctx, request, serviceUrl);
        }

        String environment = request.getHeader("environment");
        if (StringUtils.isBlank(environment)) {
            return null;
        }
        String serviceUrl = environmentCacheService.getServiceUrl(request);
        return forwardRequest(ctx, request, serviceUrl);
    }

    private Object forwardRequest(RequestContext ctx, HttpServletRequest request, String serviceUrl) {
        ctx.put(REQUEST_URI_KEY, request.getServletPath());
        try {
            // always forward request to https
            if (serviceUrl.contains("8080")) {
                serviceUrl = serviceUrl.replace("8080", "8443").replace("http", "https");
            }

            Map<String, String> authHeader = pulsarAdminService.getAuthHeader(serviceUrl);
            authHeader.entrySet().forEach(entry -> ctx.addZuulRequestHeader(entry.getKey(), entry.getValue()));
            ctx.setRouteHost(new URL(serviceUrl));
            pulsarEvent.parsePulsarEvent(request.getServletPath(), request);
            log.info("Forward request to {} @ path {}",
                    serviceUrl, request.getServletPath());
        } catch (MalformedURLException e) {
            log.error("Route forward to {} path {} error: {}",
                    serviceUrl, request.getServletPath(), e.getMessage());
        }
        return null;
    }
}
