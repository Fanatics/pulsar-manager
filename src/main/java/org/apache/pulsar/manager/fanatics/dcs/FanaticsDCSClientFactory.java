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

package org.apache.pulsar.manager.fanatics.dcs;

import com.fanatics.dcs.client.DcsClientConfig;
import com.fanatics.dcs.client.DcsClientFactory;
import com.fanatics.dcs.client.rest.DcsRestClient;

import java.io.IOException;

public class FanaticsDCSClientFactory extends DcsClientFactory {

    private static final FanaticsDCSClientFactory FACTORY = new FanaticsDCSClientFactory();

    private DcsRestClient dcsRestClient = null;

    private DcsClientConfig dcsClientConfig = null;

    public static FanaticsDCSClientFactory factory() {
        return FACTORY;
    }
    public DcsRestClient getDcsRestClient() throws IOException {
        if (dcsRestClient == null) {
            String configResource = String.format("dcs-%s.yml", System.getenv().getOrDefault("ENV_GROUP", "ecomdev"));
            synchronized (this) {
                if (dcsRestClient == null) {
                    dcsClientConfig = configFromResource(configResource);
                    dcsRestClient = restClient(dcsClientConfig);
                }
            }
        }
        return dcsRestClient;
    }

    public DcsClientConfig getDcsClientConfig() { return dcsClientConfig; }
}
